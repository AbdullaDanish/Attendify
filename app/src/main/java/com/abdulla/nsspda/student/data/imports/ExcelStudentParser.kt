package com.abdulla.nsspda.student.data.imports

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.abdulla.nsspda.di.IoDispatcher
import java.io.File
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.apache.poi.EncryptedDocumentException
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory


private data class CellPosition(
    val rowIndex: Int,
    val columnIndex: Int
)
private typealias MergedCellLookup =
        Map<CellPosition, CellPosition>
class ExcelStudentParser @Inject constructor(
    private val contentResolver: ContentResolver,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) {

    private companion object {

        const val MAX_FILE_SIZE_BYTES =
            25L * 1024L * 1024L

        const val MAX_SHEETS = 20

        /*
         * Most academic workbooks place the actual table
         * within the first few rows. Forty rows still allows
         * title blocks and institutional information.
         */
        const val MAX_ROWS_TO_SCAN_FOR_HEADER = 40

        /*
         * Name and USN columns generally appear well before
         * column BH. Keeping this bounded prevents marks
         * workbooks from becoming expensive to analyze.
         */
        const val MAX_COLUMNS_TO_SCAN = 60

        const val HEADER_LEVEL_COUNT = 3

        const val SAMPLE_ROWS_PER_COLUMN = 12

        const val MAX_ROWS_AFTER_HEADER_TO_FIND_DATA = 20

        const val MINIMUM_PAIR_CONFIDENCE = 0.45f
        const val MINIMUM_COLUMN_CONFIDENCE = 0.25f

        const val HEADER_WEIGHT = 0.75f
        const val DATA_WEIGHT = 0.25f

        const val PAIR_COLUMN_WEIGHT = 0.80f
        const val PAIR_AGREEMENT_WEIGHT = 0.20f

        const val MAX_NAME_CANDIDATES = 4
        const val MAX_USN_CANDIDATES = 4

        const val STRONG_HEADER_SCORE = 0.90f
        const val STRONG_RESULT_CONFIDENCE = 0.90f
    }

    suspend fun analyze(
        uri: Uri
    ): List<HeaderDetectionResult> =
        withContext(ioDispatcher) {
            val metadata = readMetadata(uri)
            validateMetadata(metadata)

            withWorkbook(uri) { workbook ->
                require(workbook.numberOfSheets > 0) {
                    "The workbook does not contain any sheets."
                }

                val formatter =
                    DataFormatter(Locale.getDefault())

                val evaluator =
                    workbook.creationHelper
                        .createFormulaEvaluator()

                val results =
                    mutableListOf<HeaderDetectionResult>()

                val sheetCount = minOf(
                    workbook.numberOfSheets,
                    MAX_SHEETS
                )

                for (
                sheetIndex in 0 until sheetCount
                ) {
                    val sheet =
                        workbook.getSheetAt(sheetIndex)

                    val detectionResult =
                        detectHeader(
                            sheetIndex = sheetIndex,
                            sheet = sheet,
                            formatter = formatter,
                            evaluator = evaluator
                        )

                    if (detectionResult != null) {
                        results += detectionResult
                    }
                }

                results.sortedByDescending {
                    it.confidence
                }
            }
        }

    suspend fun parsePreview(
        uri: Uri,
        mapping: StudentImportMapping
    ): StudentImportPreview =
        withContext(ioDispatcher) {
            val metadata = readMetadata(uri)
            validateMetadata(metadata)

            withWorkbook(uri) { workbook ->
                require(
                    mapping.sheetIndex in
                            0 until workbook.numberOfSheets
                ) {
                    "The selected sheet is unavailable."
                }

                require(
                    mapping.nameColumnIndex !=
                            mapping.usnColumnIndex
                ) {
                    "Name and USN must use different columns."
                }

                val formatter =
                    DataFormatter(Locale.getDefault())

                val evaluator =
                    workbook.creationHelper
                        .createFormulaEvaluator()

                val sheet =
                    workbook.getSheetAt(
                        mapping.sheetIndex
                    )

                val validRows =
                    mutableListOf<ParsedStudentRow>()

                val invalidRows =
                    mutableListOf<InvalidStudentRow>()

                val seenUsns =
                    mutableSetOf<String>()

                val firstDataRow =
                    findFirstDataRow(
                        sheet = sheet,
                        mapping = mapping,
                        formatter = formatter,
                        evaluator = evaluator
                    )

                if (
                    firstDataRow <=
                    sheet.lastRowNum
                ) {
                    for (
                    rowIndex in
                    firstDataRow..sheet.lastRowNum
                    ) {
                        val row =
                            sheet.getRow(rowIndex)
                                ?: continue

                        val rawName = readCell(
                            row = row,
                            columnIndex =
                                mapping.nameColumnIndex,
                            formatter = formatter,
                            evaluator = evaluator
                        )

                        val rawUsn = readCell(
                            row = row,
                            columnIndex =
                                mapping.usnColumnIndex,
                            formatter = formatter,
                            evaluator = evaluator
                        )

                        if (
                            rawName.isBlank() &&
                            rawUsn.isBlank()
                        ) {
                            continue
                        }

                        /*
                         * Some workbooks repeat headers after
                         * page breaks or between groups.
                         */
                        if (
                            isKnownHeaderText(rawName) ||
                            isKnownHeaderText(rawUsn)
                        ) {
                            continue
                        }

                        val normalizedName =
                            normalizeStudentName(
                                rawName
                            )

                        val normalizedUsn =
                            normalizeUsn(
                                rawUsn
                            )

                        val reason = when {
                            normalizedName.isBlank() ->
                                InvalidStudentReason
                                    .MISSING_NAME

                            normalizedUsn.isBlank() ->
                                InvalidStudentReason
                                    .MISSING_USN

                            !isProbableStudentName(
                                normalizedName
                            ) ->
                                InvalidStudentReason
                                    .INVALID_NAME

                            !isProbableStudentIdentifier(
                                normalizedUsn
                            ) ->
                                InvalidStudentReason
                                    .INVALID_USN

                            !seenUsns.add(
                                normalizedUsn
                            ) ->
                                InvalidStudentReason
                                    .DUPLICATE_USN_IN_FILE

                            else -> null
                        }

                        if (reason == null) {
                            validRows +=
                                ParsedStudentRow(
                                    sourceRowNumber =
                                        rowIndex + 1,
                                    studentName =
                                        normalizedName,
                                    usn =
                                        normalizedUsn
                                )
                        } else {
                            invalidRows +=
                                InvalidStudentRow(
                                    sourceRowNumber =
                                        rowIndex + 1,
                                    rawName =
                                        rawName,
                                    rawUsn =
                                        rawUsn,
                                    reason =
                                        reason
                                )
                        }
                    }
                }

                StudentImportPreview(
                    metadata = metadata,
                    sheets =
                        createSheetSummaries(
                            workbook
                        ),
                    mapping = mapping,
                    detectedConfidence =
                        mapping.detectionConfidence,
                    validRows = validRows,
                    invalidRows = invalidRows
                )
            }
        }

    private fun detectHeader(
        sheetIndex: Int,
        sheet: Sheet,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): HeaderDetectionResult? {
        if (sheet.physicalNumberOfRows <= 0) {
            return null
        }

        val firstRowIndex =
            sheet.firstRowNum
                .coerceAtLeast(0)

        val finalRowIndex = minOf(
            sheet.lastRowNum,
            firstRowIndex +
                    MAX_ROWS_TO_SCAN_FOR_HEADER -
                    1
        )

        val mergedCellLookup =
            buildMergedCellLookup(
                sheet = sheet,
                minimumRowIndex =
                    firstRowIndex,
                maximumRowIndex =
                    minOf(
                        sheet.lastRowNum,
                        finalRowIndex +
                                HEADER_LEVEL_COUNT
                    )
            )

        var bestResult:
                HeaderDetectionResult? = null

        for (
        rowIndex in
        firstRowIndex..finalRowIndex
        ) {
            val row =
                sheet.getRow(rowIndex)
                    ?: continue

            if (
                row.physicalNumberOfCells == 0
            ) {
                continue
            }

            if (
                !hasAtLeastTwoUsefulCells(
                    row = row,
                    formatter = formatter,
                    evaluator = evaluator
                )
            ) {
                continue
            }

            val finalColumnExclusive =
                determineFinalColumnExclusive(
                    row = row,
                    sheet = sheet,
                    rowIndex = rowIndex
                )

            if (finalColumnExclusive < 2) {
                continue
            }

            val availableColumns =
                mutableListOf<SpreadsheetColumn>()

            val nameCandidates =
                mutableListOf<ColumnCandidate>()

            val usnCandidates =
                mutableListOf<ColumnCandidate>()

            for (
            columnIndex in
            0 until finalColumnExclusive
            ) {
                val headerText =
                    readCombinedHeaderText(
                        sheet = sheet,
                        headerRowIndex =
                            rowIndex,
                        columnIndex =
                            columnIndex,
                        mergedCellLookup =
                            mergedCellLookup,
                        formatter =
                            formatter,
                        evaluator =
                            evaluator
                    )

                if (headerText.isNotBlank()) {
                    availableColumns +=
                        SpreadsheetColumn(
                            index =
                                columnIndex,
                            displayName =
                                headerText
                        )
                }

                val nameHeaderScore =
                    ExcelHeaderDetector
                        .scoreNameHeader(
                            headerText
                        )

                val usnHeaderScore =
                    ExcelHeaderDetector
                        .scoreUsnHeader(
                            headerText
                        )

                val needsNameSamples =
                    nameHeaderScore <
                            STRONG_HEADER_SCORE

                val needsUsnSamples =
                    usnHeaderScore <
                            STRONG_HEADER_SCORE

                /*
                 * For wide marks sheets, only inspect data
                 * when a header is potentially relevant.
                 * For small sheets, inspect all columns so
                 * headerless files can still be detected.
                 */
                val potentiallyRelevant =
                    nameHeaderScore > 0f ||
                            usnHeaderScore > 0f ||
                            headerText.isBlank() ||
                            finalColumnExclusive <= 12

                val samples =
                    if (
                        potentiallyRelevant &&
                        (
                                needsNameSamples ||
                                        needsUsnSamples
                                )
                    ) {
                        readSamples(
                            sheet = sheet,
                            headerRowIndex =
                                rowIndex,
                            columnIndex =
                                columnIndex,
                            formatter =
                                formatter,
                            evaluator =
                                evaluator
                        )
                    } else {
                        emptyList()
                    }

                val nameDataScore =
                    when {
                        !needsNameSamples -> 1f

                        else ->
                            scoreNameSamples(
                                samples
                            )
                    }

                val usnDataScore =
                    when {
                        !needsUsnSamples -> 1f

                        else ->
                            scoreUsnSamples(
                                samples
                            )
                    }

                val nameConfidence =
                    combineScores(
                        headerScore =
                            nameHeaderScore,
                        dataScore =
                            nameDataScore
                    )

                val usnConfidence =
                    combineScores(
                        headerScore =
                            usnHeaderScore,
                        dataScore =
                            usnDataScore
                    )

                if (
                    nameConfidence >=
                    MINIMUM_COLUMN_CONFIDENCE
                ) {
                    nameCandidates +=
                        ColumnCandidate(
                            columnIndex =
                                columnIndex,
                            headerValue =
                                headerText.ifBlank {
                                    columnLabel(
                                        columnIndex
                                    )
                                },
                            confidence =
                                nameConfidence
                        )
                }

                if (
                    usnConfidence >=
                    MINIMUM_COLUMN_CONFIDENCE
                ) {
                    usnCandidates +=
                        ColumnCandidate(
                            columnIndex =
                                columnIndex,
                            headerValue =
                                headerText.ifBlank {
                                    columnLabel(
                                        columnIndex
                                    )
                                },
                            confidence =
                                usnConfidence
                        )
                }
            }

            val strongestNameCandidates =
                nameCandidates
                    .sortedByDescending {
                        it.confidence
                    }
                    .take(
                        MAX_NAME_CANDIDATES
                    )

            val strongestUsnCandidates =
                usnCandidates
                    .sortedByDescending {
                        it.confidence
                    }
                    .take(
                        MAX_USN_CANDIDATES
                    )

            val bestPair =
                findBestColumnPair(
                    nameCandidates =
                        strongestNameCandidates,
                    usnCandidates =
                        strongestUsnCandidates,
                    sheet =
                        sheet,
                    headerRowIndex =
                        rowIndex,
                    formatter =
                        formatter,
                    evaluator =
                        evaluator
                ) ?: continue

            if (
                bestPair.confidence <
                MINIMUM_PAIR_CONFIDENCE
            ) {
                continue
            }

            val candidate =
                HeaderDetectionResult(
                    sheetIndex =
                        sheetIndex,
                    sheetName =
                        sheet.sheetName,
                    headerRowIndex =
                        rowIndex,
                    availableColumns =
                        availableColumns.ifEmpty {
                            buildFallbackColumns(
                                finalColumnExclusive
                            )
                        },
                    nameColumn =
                        bestPair.nameColumn,
                    usnColumn =
                        bestPair.usnColumn,
                    confidence =
                        bestPair.confidence
                )

            if (
                bestResult == null ||
                candidate.confidence >
                bestResult.confidence
            ) {
                bestResult = candidate
            }

            /*
             * A clear name-USN pair has already been found.
             * Continuing through all later rows would only
             * increase processing time.
             */
            if (
                candidate.confidence >=
                STRONG_RESULT_CONFIDENCE
            ) {
                return candidate
            }
        }

        return bestResult
    }

    private data class StudentColumnPair(
        val nameColumn: ColumnCandidate,
        val usnColumn: ColumnCandidate,
        val confidence: Float
    )

    private fun findBestColumnPair(
        nameCandidates: List<ColumnCandidate>,
        usnCandidates: List<ColumnCandidate>,
        sheet: Sheet,
        headerRowIndex: Int,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): StudentColumnPair? {
        if (
            nameCandidates.isEmpty() ||
            usnCandidates.isEmpty()
        ) {
            return null
        }

        var bestPair:
                StudentColumnPair? = null

        for (
        nameCandidate in
        nameCandidates
        ) {
            for (
            usnCandidate in
            usnCandidates
            ) {
                if (
                    nameCandidate.columnIndex ==
                    usnCandidate.columnIndex
                ) {
                    continue
                }

                val rowAgreementScore =
                    scoreStudentRowAgreement(
                        sheet =
                            sheet,
                        headerRowIndex =
                            headerRowIndex,
                        nameColumnIndex =
                            nameCandidate
                                .columnIndex,
                        usnColumnIndex =
                            usnCandidate
                                .columnIndex,
                        formatter =
                            formatter,
                        evaluator =
                            evaluator
                    )

                val averageColumnConfidence =
                    (
                            nameCandidate.confidence +
                                    usnCandidate.confidence
                            ) / 2f

                val pairConfidence =
                    (
                            averageColumnConfidence *
                                    PAIR_COLUMN_WEIGHT +
                                    rowAgreementScore *
                                    PAIR_AGREEMENT_WEIGHT
                            ).coerceIn(
                            0f,
                            1f
                        )

                val pair =
                    StudentColumnPair(
                        nameColumn =
                            nameCandidate,
                        usnColumn =
                            usnCandidate,
                        confidence =
                            pairConfidence
                    )

                if (
                    bestPair == null ||
                    pair.confidence >
                    bestPair.confidence
                ) {
                    bestPair = pair
                }
            }
        }

        return bestPair
    }

    private fun scoreStudentRowAgreement(
        sheet: Sheet,
        headerRowIndex: Int,
        nameColumnIndex: Int,
        usnColumnIndex: Int,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): Float {
        var checkedRows = 0
        var matchingRows = 0

        val finalRow = minOf(
            sheet.lastRowNum,
            headerRowIndex +
                    MAX_ROWS_AFTER_HEADER_TO_FIND_DATA +
                    SAMPLE_ROWS_PER_COLUMN
        )

        if (
            headerRowIndex + 1 >
            finalRow
        ) {
            return 0f
        }

        for (
        rowIndex in
        headerRowIndex + 1..finalRow
        ) {
            val row =
                sheet.getRow(rowIndex)
                    ?: continue

            val name =
                readCell(
                    row = row,
                    columnIndex =
                        nameColumnIndex,
                    formatter =
                        formatter,
                    evaluator =
                        evaluator
                )

            val usn =
                readCell(
                    row = row,
                    columnIndex =
                        usnColumnIndex,
                    formatter =
                        formatter,
                    evaluator =
                        evaluator
                )

            if (
                name.isBlank() &&
                usn.isBlank()
            ) {
                continue
            }

            checkedRows++

            if (
                isProbableStudentName(name) &&
                isProbableStudentIdentifier(usn)
            ) {
                matchingRows++
            }

            if (
                checkedRows >=
                SAMPLE_ROWS_PER_COLUMN
            ) {
                break
            }
        }

        if (checkedRows == 0) {
            return 0f
        }

        return matchingRows.toFloat() /
                checkedRows.toFloat()
    }

    private fun scoreNameSamples(
        samples: List<String>
    ): Float {
        if (samples.isEmpty()) {
            return 0f
        }

        val matchingCount =
            samples.count(
                ::isProbableStudentName
            )

        return matchingCount.toFloat() /
                samples.size.toFloat()
    }

    private fun scoreUsnSamples(
        samples: List<String>
    ): Float {
        if (samples.isEmpty()) {
            return 0f
        }

        val matchingCount =
            samples.count(
                ::isProbableStudentIdentifier
            )

        return matchingCount.toFloat() /
                samples.size.toFloat()
    }

    private fun combineScores(
        headerScore: Float,
        dataScore: Float
    ): Float {
        return (
                headerScore * HEADER_WEIGHT +
                        dataScore * DATA_WEIGHT
                ).coerceIn(
                0f,
                1f
            )
    }

    private fun findFirstDataRow(
        sheet: Sheet,
        mapping: StudentImportMapping,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): Int {
        if (
            sheet.lastRowNum <=
            mapping.headerRowIndex
        ) {
            return mapping.headerRowIndex + 1
        }

        val searchStart =
            (mapping.headerRowIndex + 1)
                .coerceAtMost(
                    sheet.lastRowNum
                )

        val searchEnd = minOf(
            sheet.lastRowNum,
            mapping.headerRowIndex +
                    MAX_ROWS_AFTER_HEADER_TO_FIND_DATA
        )

        if (searchStart > searchEnd) {
            return mapping.headerRowIndex + 1
        }

        for (
        rowIndex in
        searchStart..searchEnd
        ) {
            val row =
                sheet.getRow(rowIndex)
                    ?: continue

            val name =
                readCell(
                    row = row,
                    columnIndex =
                        mapping.nameColumnIndex,
                    formatter =
                        formatter,
                    evaluator =
                        evaluator
                )

            val usn =
                readCell(
                    row = row,
                    columnIndex =
                        mapping.usnColumnIndex,
                    formatter =
                        formatter,
                    evaluator =
                        evaluator
                )

            if (
                isProbableStudentName(name) &&
                isProbableStudentIdentifier(usn)
            ) {
                return rowIndex
            }
        }

        /*
         * Preview validation still skips empty rows,
         * repeated headers and invalid metadata rows.
         */
        return mapping.headerRowIndex + 1
    }

    private fun readSamples(
        sheet: Sheet,
        headerRowIndex: Int,
        columnIndex: Int,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): List<String> {
        val firstRow =
            headerRowIndex + 1

        val finalRow = minOf(
            sheet.lastRowNum,
            headerRowIndex +
                    SAMPLE_ROWS_PER_COLUMN
        )

        if (firstRow > finalRow) {
            return emptyList()
        }

        val values =
            ArrayList<String>(
                SAMPLE_ROWS_PER_COLUMN
            )

        for (
        rowIndex in
        firstRow..finalRow
        ) {
            val row =
                sheet.getRow(rowIndex)
                    ?: continue

            val value =
                readCell(
                    row = row,
                    columnIndex =
                        columnIndex,
                    formatter =
                        formatter,
                    evaluator =
                        evaluator
                )

            if (value.isNotBlank()) {
                values += value
            }
        }

        return values
    }



    private fun buildMergedCellLookup(
        sheet: Sheet,
        minimumRowIndex: Int,
        maximumRowIndex: Int
    ): MergedCellLookup {
        if (sheet.numMergedRegions == 0) {
            return emptyMap()
        }

        val result =
            HashMap<
                    CellPosition,
                    CellPosition
                    >()

        for (
        regionIndex in
        0 until sheet.numMergedRegions
        ) {
            val region =
                sheet.getMergedRegion(
                    regionIndex
                )

            if (
                region.lastRow <
                minimumRowIndex ||
                region.firstRow >
                maximumRowIndex
            ) {
                continue
            }

            if (
                region.firstColumn >=
                MAX_COLUMNS_TO_SCAN
            ) {
                continue
            }

            val source =
                CellPosition(
                    rowIndex =
                        region.firstRow,
                    columnIndex =
                        region.firstColumn
                )

            val startRow =
                maxOf(
                    region.firstRow,
                    minimumRowIndex
                )

            val endRow =
                minOf(
                    region.lastRow,
                    maximumRowIndex
                )

            val endColumn =
                minOf(
                    region.lastColumn,
                    MAX_COLUMNS_TO_SCAN - 1
                )

            for (
            rowIndex in
            startRow..endRow
            ) {
                for (
                columnIndex in
                region.firstColumn..endColumn
                ) {
                    result[
                        CellPosition(
                            rowIndex =
                                rowIndex,
                            columnIndex =
                                columnIndex
                        )
                    ] = source
                }
            }
        }

        return result
    }

    private fun readCombinedHeaderText(
        sheet: Sheet,
        headerRowIndex: Int,
        columnIndex: Int,
        mergedCellLookup: MergedCellLookup,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): String {
        val values =
            linkedSetOf<String>()

        val finalHeaderRow = minOf(
            sheet.lastRowNum,
            headerRowIndex +
                    HEADER_LEVEL_COUNT -
                    1
        )

        for (
        rowIndex in
        headerRowIndex..finalHeaderRow
        ) {
            val row =
                sheet.getRow(rowIndex)

            if (row != null) {
                val directValue =
                    readCell(
                        row = row,
                        columnIndex =
                            columnIndex,
                        formatter =
                            formatter,
                        evaluator =
                            evaluator
                    )

                if (
                    directValue.isNotBlank()
                ) {
                    values += directValue
                }
            }

            val mergedValue =
                readMergedCellValue(
                    sheet = sheet,
                    rowIndex =
                        rowIndex,
                    columnIndex =
                        columnIndex,
                    mergedCellLookup =
                        mergedCellLookup,
                    formatter =
                        formatter,
                    evaluator =
                        evaluator
                )

            if (
                mergedValue.isNotBlank()
            ) {
                values += mergedValue
            }
        }

        return values.joinToString(
            separator = " "
        )
    }

    private fun readMergedCellValue(
        sheet: Sheet,
        rowIndex: Int,
        columnIndex: Int,
        mergedCellLookup: MergedCellLookup,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): String {
        val requestedPosition =
            CellPosition(
                rowIndex =
                    rowIndex,
                columnIndex =
                    columnIndex
            )

        val sourcePosition =
            mergedCellLookup[
                requestedPosition
            ] ?: return ""

        /*
         * The source cell was already read directly.
         */
        if (
            sourcePosition ==
            requestedPosition
        ) {
            return ""
        }

        val sourceRow =
            sheet.getRow(
                sourcePosition.rowIndex
            ) ?: return ""

        return readCell(
            row = sourceRow,
            columnIndex =
                sourcePosition.columnIndex,
            formatter = formatter,
            evaluator = evaluator
        )
    }

    private fun determineFinalColumnExclusive(
        row: Row,
        sheet: Sheet,
        rowIndex: Int
    ): Int {
        var maximumColumn =
            row.lastCellNum
                .toInt()
                .coerceAtLeast(0)

        val finalRelatedRow = minOf(
            sheet.lastRowNum,
            rowIndex +
                    HEADER_LEVEL_COUNT -
                    1
        )

        for (
        relatedRowIndex in
        rowIndex..finalRelatedRow
        ) {
            val relatedRow =
                sheet.getRow(
                    relatedRowIndex
                ) ?: continue

            maximumColumn = maxOf(
                maximumColumn,
                relatedRow.lastCellNum
                    .toInt()
                    .coerceAtLeast(0)
            )
        }

        return maximumColumn
            .coerceAtMost(
                MAX_COLUMNS_TO_SCAN
            )
    }

    private fun hasAtLeastTwoUsefulCells(
        row: Row,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): Boolean {
        var usefulCells = 0

        val finalColumnExclusive =
            row.lastCellNum
                .toInt()
                .coerceAtLeast(0)
                .coerceAtMost(
                    MAX_COLUMNS_TO_SCAN
                )

        for (
        columnIndex in
        0 until finalColumnExclusive
        ) {
            val value =
                readCell(
                    row = row,
                    columnIndex =
                        columnIndex,
                    formatter =
                        formatter,
                    evaluator =
                        evaluator
                )

            if (value.isNotBlank()) {
                usefulCells++

                if (usefulCells >= 2) {
                    return true
                }
            }
        }

        return false
    }

    private fun readCell(
        row: Row,
        columnIndex: Int,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): String {
        val cell =
            row.getCell(
                columnIndex,
                Row.MissingCellPolicy
                    .RETURN_BLANK_AS_NULL
            ) ?: return ""

        return runCatching {
            when (cell.cellType) {
                CellType.FORMULA -> {
                    formatter.formatCellValue(
                        cell,
                        evaluator
                    )
                }

                else -> {
                    formatter.formatCellValue(
                        cell
                    )
                }
            }
        }.getOrElse {
            runCatching {
                formatter.formatCellValue(
                    cell
                )
            }.getOrDefault("")
        }.trim()
    }

    private fun isProbableStudentName(
        rawValue: String
    ): Boolean {
        val value =
            normalizeStudentName(
                rawValue
            )

        if (value.isBlank()) {
            return false
        }

        if (
            isKnownNonStudentText(value)
        ) {
            return false
        }

        val letters =
            value.count(
                Char::isLetter
            )

        val digits =
            value.count(
                Char::isDigit
            )

        return value.length in 2..150 &&
                letters >= 2 &&
                letters >= digits &&
                !value.matches(
                    Regex(
                        "^[\\d.,+\\-/% ]+$"
                    )
                )
    }

    private fun isProbableStudentIdentifier(
        rawValue: String
    ): Boolean {
        val value =
            normalizeUsn(
                rawValue
            )

        if (value.isBlank()) {
            return false
        }

        if (isKnownHeaderText(value)) {
            return false
        }

        if (value.length !in 2..60) {
            return false
        }

        if (
            value.any {
                it == ',' ||
                        it == ';' ||
                        it == '\n'
            }
        ) {
            return false
        }

        val hasLetter =
            value.any(
                Char::isLetter
            )

        val hasDigit =
            value.any(
                Char::isDigit
            )

        /*
         * Supports:
         *
         * 3PD21CS001
         * 001
         * ROLL001
         * A-102
         */
        return hasDigit ||
                (
                        hasLetter &&
                                value.length >= 3 &&
                                !value.contains(' ')
                        )
    }

    private fun isKnownHeaderText(
        value: String
    ): Boolean {
        val normalized =
            ExcelHeaderDetector
                .normalizeHeader(
                    value
                )

        if (normalized.isBlank()) {
            return false
        }

        return ExcelHeaderDetector
            .scoreNameHeader(
                normalized
            ) >= 0.65f ||
                ExcelHeaderDetector
                    .scoreUsnHeader(
                        normalized
                    ) >= 0.65f ||
                normalized in knownHeaders
    }

    private fun isKnownNonStudentText(
        value: String
    ): Boolean {
        val normalized =
            ExcelHeaderDetector
                .normalizeHeader(
                    value
                )

        return normalized in
                knownNonStudentTexts ||
                normalized.contains(
                    "department"
                ) ||
                normalized.contains(
                    "college"
                ) ||
                normalized.contains(
                    "university"
                ) ||
                normalized.contains(
                    "institution"
                ) ||
                normalized.contains(
                    "faculty"
                ) ||
                normalized.contains(
                    "course"
                ) ||
                normalized.contains(
                    "subject"
                )
    }

    private val knownHeaders = setOf(
        "sl no",
        "s no",
        "serial no",
        "serial number",
        "remarks",
        "marks",
        "total",
        "percentage",
        "result",
        "grade"
    )

    private val knownNonStudentTexts = setOf(
        "name",
        "student name",
        "candidate name",
        "marks in words",
        "eligible",
        "not eligible",
        "remarks",
        "total",
        "result",
        "grade"
    )

    private fun normalizeStudentName(
        value: String
    ): String {
        return value
            .trim()
            .replace(
                regex =
                    Regex("\\s+"),
                replacement = " "
            )
    }

    private fun normalizeUsn(
        value: String
    ): String {
        val trimmed =
            value
                .trim()
                .removeSuffix(".0")
                .trim()

        return trimmed
            .uppercase(Locale.ROOT)
            .replace(
                Regex("\\s+"),
                ""
            )
            .replace(
                Regex("[‐-‒–—]"),
                "-"
            )
    }

    private fun buildFallbackColumns(
        count: Int
    ): List<SpreadsheetColumn> {
        return (0 until count).map {
                columnIndex ->
            SpreadsheetColumn(
                index = columnIndex,
                displayName =
                    columnLabel(
                        columnIndex
                    )
            )
        }
    }

    private fun columnLabel(
        columnIndex: Int
    ): String {
        var value =
            columnIndex + 1

        val result =
            StringBuilder()

        while (value > 0) {
            val remainder =
                (value - 1) % 26

            result.append(
                (
                        'A'.code +
                                remainder
                        ).toChar()
            )

            value =
                (value - 1) / 26
        }

        return "Column ${result.reverse()}"
    }

    private fun createSheetSummaries(
        workbook: Workbook
    ): List<SheetSummary> {
        val summaries =
            ArrayList<SheetSummary>(
                workbook.numberOfSheets
            )

        for (
        index in
        0 until workbook.numberOfSheets
        ) {
            val sheet =
                workbook.getSheetAt(index)

            summaries +=
                SheetSummary(
                    index = index,
                    name =
                        sheet.sheetName,
                    firstRowIndex =
                        sheet.firstRowNum,
                    lastRowIndex =
                        sheet.lastRowNum
                )
        }

        return summaries
    }

    private fun validateMetadata(
        metadata: SpreadsheetMetadata
    ) {
        val size =
            metadata.sizeBytes

        require(
            size == null ||
                    size <=
                    MAX_FILE_SIZE_BYTES
        ) {
            "The selected file is larger than 25 MB."
        }
    }

    private fun readMetadata(
        uri: Uri
    ): SpreadsheetMetadata {
        var displayName =
            "Selected workbook"

        var size: Long? = null

        contentResolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            ),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex =
                    cursor.getColumnIndex(
                        OpenableColumns
                            .DISPLAY_NAME
                    )

                val sizeIndex =
                    cursor.getColumnIndex(
                        OpenableColumns.SIZE
                    )

                if (
                    nameIndex >= 0 &&
                    !cursor.isNull(nameIndex)
                ) {
                    displayName =
                        cursor.getString(
                            nameIndex
                        )
                }

                if (
                    sizeIndex >= 0 &&
                    !cursor.isNull(sizeIndex)
                ) {
                    size =
                        cursor.getLong(
                            sizeIndex
                        )
                }
            }
        }

        return SpreadsheetMetadata(
            displayName =
                displayName,
            sizeBytes =
                size,
            mimeType =
                contentResolver.getType(
                    uri
                )
        )
    }

    private fun <T> withWorkbook(
        uri: Uri,
        block: (Workbook) -> T
    ): T {
        val temporaryFile =
            File.createTempFile(
                "student_import_",
                ".workbook"
            )

        return try {
            contentResolver
                .openInputStream(uri)
                ?.buffered()
                ?.use { inputStream ->
                    temporaryFile
                        .outputStream()
                        .buffered()
                        .use { outputStream ->
                            inputStream.copyTo(
                                outputStream
                            )
                        }
                }
                ?: throw IllegalStateException(
                    "Unable to open the selected file."
                )

            WorkbookFactory.create(
                temporaryFile,
                null,
                true
            ).use { workbook ->
                block(workbook)
            }
        } catch (
            exception:
            EncryptedDocumentException
        ) {
            throw IllegalArgumentException(
                "Password-protected Excel files are not supported.",
                exception
            )
        } catch (
            exception: Exception
        ) {
            if (
                exception is
                        IllegalArgumentException
            ) {
                throw exception
            }

            throw IllegalArgumentException(
                "The selected file is not a valid or supported Excel workbook.",
                exception
            )
        } finally {
            runCatching {
                temporaryFile.delete()
            }
        }
    }
}
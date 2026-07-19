package com.abdulla.nsspda.student.data.imports

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.abdulla.nsspda.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.apache.poi.EncryptedDocumentException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject

class ExcelStudentParser @Inject constructor(
    private val contentResolver: ContentResolver,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher)
{

    companion object {
        private const val MAX_FILE_SIZE_BYTES =
            25L * 1024L * 1024L

        private const val MAX_SHEETS = 50
        private const val MAX_ROWS_TO_SCAN_FOR_HEADER = 50
        private const val MAX_COLUMNS_TO_SCAN = 100
        private const val SAMPLE_ROWS_PER_COLUMN = 20
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

                val formatter = DataFormatter(
                    Locale.getDefault()
                )

                val evaluator =
                    workbook.creationHelper
                        .createFormulaEvaluator()

                buildList {
                    val sheetCount =
                        minOf(
                            workbook.numberOfSheets,
                            MAX_SHEETS
                        )

                    for (
                    sheetIndex in 0 until sheetCount
                    ) {
                        val sheet =
                            workbook.getSheetAt(sheetIndex)

                        detectHeader(
                            sheetIndex = sheetIndex,
                            sheet = sheet,
                            formatter = formatter,
                            evaluator = evaluator
                        )?.let(::add)
                    }
                }.sortedByDescending {
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
                    mapping.headerRowIndex + 1

                for (
                rowIndex in firstDataRow..
                        sheet.lastRowNum
                ) {
                    val row = sheet.getRow(rowIndex)
                        ?: continue

                    val rawName =
                        readCell(
                            row = row,
                            columnIndex =
                                mapping.nameColumnIndex,
                            formatter = formatter,
                            evaluator = evaluator
                        )

                    val rawUsn =
                        readCell(
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

                    val normalizedName =
                        normalizeStudentName(rawName)

                    val normalizedUsn =
                        normalizeUsn(rawUsn)

                    val reason = when {
                        normalizedName.isBlank() ->
                            InvalidStudentReason.MISSING_NAME

                        normalizedUsn.isBlank() ->
                            InvalidStudentReason.MISSING_USN

                        !seenUsns.add(normalizedUsn) ->
                            InvalidStudentReason
                                .DUPLICATE_USN_IN_FILE

                        else -> null
                    }

                    if (reason != null) {
                        invalidRows += InvalidStudentRow(
                            sourceRowNumber =
                                rowIndex + 1,
                            rawName = rawName,
                            rawUsn = rawUsn,
                            reason = reason
                        )
                    } else {
                        validRows += ParsedStudentRow(
                            sourceRowNumber =
                                rowIndex + 1,
                            studentName =
                                normalizedName,
                            usn = normalizedUsn
                        )
                    }
                }

                StudentImportPreview(
                    metadata = metadata,
                    sheets = workbook.sheetIterator()
                        .asSequence()
                        .mapIndexed { index, sheetItem ->
                            SheetSummary(
                                index = index,
                                name = sheetItem.sheetName,
                                firstRowIndex = sheetItem.firstRowNum,
                                lastRowIndex = sheetItem.lastRowNum
                            )
                        }
                        .toList(),
                    mapping = mapping,
                    detectedConfidence = mapping.detectionConfidence,
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
        var bestResult: HeaderDetectionResult? = null

        val finalRow = minOf(
            sheet.lastRowNum,
            sheet.firstRowNum +
                    MAX_ROWS_TO_SCAN_FOR_HEADER
        )

        for (rowIndex in sheet.firstRowNum..finalRow) {
            val row = sheet.getRow(rowIndex)
                ?: continue

            var bestName: ColumnCandidate? = null
            var bestUsn: ColumnCandidate? = null

            val availableColumns =
                mutableListOf<SpreadsheetColumn>()

            val finalColumn = minOf(
                row.lastCellNum
                    .toInt()
                    .coerceAtLeast(0),
                MAX_COLUMNS_TO_SCAN
            )

            for (columnIndex in 0 until finalColumn) {
                val value = readCell(
                    row = row,
                    columnIndex = columnIndex,
                    formatter = formatter,
                    evaluator = evaluator
                )

                if (value.isBlank()) {
                    continue
                }

                availableColumns += SpreadsheetColumn(
                    index = columnIndex,
                    displayName = value
                )

                val nameScore =
                    (
                            ExcelHeaderDetector
                                .scoreNameHeader(value) +
                                    scoreNameColumnValues(
                                        sheet = sheet,
                                        headerRowIndex = rowIndex,
                                        columnIndex = columnIndex,
                                        formatter = formatter,
                                        evaluator = evaluator
                                    )
                            ).coerceIn(0f, 1f)

                val usnScore =
                    (
                            ExcelHeaderDetector
                                .scoreUsnHeader(value) +
                                    scoreUsnColumnValues(
                                        sheet = sheet,
                                        headerRowIndex = rowIndex,
                                        columnIndex = columnIndex,
                                        formatter = formatter,
                                        evaluator = evaluator
                                    )
                            ).coerceIn(0f, 1f)

                if (
                    nameScore > 0f &&
                    (
                            bestName == null ||
                                    nameScore >
                                    bestName.confidence
                            )
                ) {
                    bestName = ColumnCandidate(
                        columnIndex = columnIndex,
                        headerValue = value,
                        confidence = nameScore
                    )
                }

                if (
                    usnScore > 0f &&
                    (
                            bestUsn == null ||
                                    usnScore >
                                    bestUsn.confidence
                            )
                ) {
                    bestUsn = ColumnCandidate(
                        columnIndex = columnIndex,
                        headerValue = value,
                        confidence = usnScore
                    )
                }
            }

            if (availableColumns.size < 2) {
                continue
            }

            if (
                bestName?.columnIndex ==
                bestUsn?.columnIndex
            ) {
                continue
            }

            val confidence = listOfNotNull(
                bestName?.confidence,
                bestUsn?.confidence
            ).averageOrZero()

            val candidate =
                HeaderDetectionResult(
                    sheetIndex = sheetIndex,
                    sheetName = sheet.sheetName,
                    headerRowIndex = rowIndex,
                    availableColumns = availableColumns,
                    nameColumn = bestName,
                    usnColumn = bestUsn,
                    confidence = confidence
                )

            if (
                bestResult == null ||
                candidate.confidence >
                bestResult.confidence
            ) {
                bestResult = candidate
            }
        }

        return bestResult
    }

    private fun scoreNameColumnValues(
        sheet: Sheet,
        headerRowIndex: Int,
        columnIndex: Int,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): Float {
        val samples = readSamples(
            sheet = sheet,
            headerRowIndex = headerRowIndex,
            columnIndex = columnIndex,
            formatter = formatter,
            evaluator = evaluator
        )

        if (samples.isEmpty()) {
            return 0f
        }

        val probableNames = samples.count {
                value ->
            value.any(Char::isLetter) &&
                    value.count(Char::isLetter) >= 2 &&
                    value.length in 2..100
        }

        return (
                probableNames.toFloat() /
                        samples.size
                ) * 0.25f
    }

    private fun scoreUsnColumnValues(
        sheet: Sheet,
        headerRowIndex: Int,
        columnIndex: Int,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): Float {
        val samples = readSamples(
            sheet = sheet,
            headerRowIndex = headerRowIndex,
            columnIndex = columnIndex,
            formatter = formatter,
            evaluator = evaluator
        )

        if (samples.isEmpty()) {
            return 0f
        }

        val probableIdentifiers =
            samples.count { value ->
                val compact =
                    value.filterNot(Char::isWhitespace)

                compact.length in 3..40 &&
                        compact.any(Char::isDigit) &&
                        compact.none {
                            it == ',' || it == ';'
                        }
            }

        return (
                probableIdentifiers.toFloat() /
                        samples.size
                ) * 0.25f
    }

    private fun readSamples(
        sheet: Sheet,
        headerRowIndex: Int,
        columnIndex: Int,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): List<String> {
        val values = mutableListOf<String>()

        val finalRow =
            minOf(
                sheet.lastRowNum,
                headerRowIndex +
                        SAMPLE_ROWS_PER_COLUMN
            )

        for (
        rowIndex in
        headerRowIndex + 1..finalRow
        ) {
            val row = sheet.getRow(rowIndex)
                ?: continue

            val value = readCell(
                row = row,
                columnIndex = columnIndex,
                formatter = formatter,
                evaluator = evaluator
            )

            if (value.isNotBlank()) {
                values += value
            }
        }

        return values
    }

    private fun readCell(
        row: Row,
        columnIndex: Int,
        formatter: DataFormatter,
        evaluator: FormulaEvaluator
    ): String {
        val cell: Cell =
            row.getCell(
                columnIndex,
                Row.MissingCellPolicy
                    .RETURN_BLANK_AS_NULL
            ) ?: return ""

        return runCatching {
            formatter.formatCellValue(
                cell,
                evaluator
            )
        }.getOrElse {
            formatter.formatCellValue(cell)
        }.trim()
    }

    private fun normalizeStudentName(
        value: String
    ): String {
        return value
            .trim()
            .replace(
                regex = Regex("\\s+"),
                replacement = " "
            )
    }

    private fun normalizeUsn(
        value: String
    ): String {
        return value
            .trim()
            .uppercase(Locale.ROOT)
            .filterNot(Char::isWhitespace)
            .removeSuffix(".0")
    }

    private fun validateMetadata(
        metadata: SpreadsheetMetadata
    ) {
        val size = metadata.sizeBytes

        require(
            size == null ||
                    size <= MAX_FILE_SIZE_BYTES
        ) {
            "The selected file is larger than 25 MB."
        }
    }

    private fun readMetadata(
        uri: Uri
    ): SpreadsheetMetadata {
        var displayName = "Selected workbook"
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
                        OpenableColumns.DISPLAY_NAME
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
                        cursor.getString(nameIndex)
                }

                if (
                    sizeIndex >= 0 &&
                    !cursor.isNull(sizeIndex)
                ) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }

        return SpreadsheetMetadata(
            displayName = displayName,
            sizeBytes = size,
            mimeType =
                contentResolver.getType(uri)
        )
    }

    private fun <T> withWorkbook(
        uri: Uri,
        block: (Workbook) -> T
    ): T {
        val temporaryFile = File.createTempFile(
            "student_import_",
            ".tmp"
        )

        return try {
            contentResolver
                .openInputStream(uri)
                ?.use { inputStream ->
                    FileOutputStream(temporaryFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
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
        } catch (exception: EncryptedDocumentException) {
            throw IllegalArgumentException(
                "Password-protected workbooks are not supported.",
                exception
            )
        } finally {
            temporaryFile.delete()
        }
    }

    private fun List<Float>.averageOrZero(): Float {
        return if (isEmpty()) {
            0f
        } else {
            average().toFloat()
        }
    }
}
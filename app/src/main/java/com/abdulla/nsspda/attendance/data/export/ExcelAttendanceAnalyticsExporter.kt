package com.abdulla.nsspda.attendance.data.export

import android.content.Context
import androidx.core.content.FileProvider
import com.abdulla.nsspda.attendance.domain.model.AttendanceAnalytics
import com.abdulla.nsspda.attendance.domain.model.export.AttendanceAnalyticsExportResult
import com.abdulla.nsspda.attendance.domain.model.export.AttendanceAnalyticsExporter
import com.abdulla.nsspda.attendance.domain.model.AttendanceRiskLevel
import com.abdulla.nsspda.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExcelAttendanceAnalyticsExporter @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : AttendanceAnalyticsExporter {

    override suspend fun exportAnalytics(
        analytics: AttendanceAnalytics,
        semester: String,
        branch: String,
        subject: String
    ): AttendanceAnalyticsExportResult {
        return withContext(ioDispatcher) {
            require(analytics.hasData) {
                "There is no attendance analytics data to export."
            }

            require(semester.isNotBlank()) {
                "Semester is missing."
            }

            require(branch.isNotBlank()) {
                "Branch is missing."
            }

            require(subject.isNotBlank()) {
                "Subject is missing."
            }

            val exportDirectory = File(
                context.cacheDir,
                EXPORT_DIRECTORY
            ).apply {
                if (!exists() && !mkdirs()) {
                    throw IllegalStateException(
                        "Unable to create the analytics export directory."
                    )
                }
            }

            removeExpiredExports(
                directory = exportDirectory
            )

            val fileName = createFileName(
                semester = semester,
                branch = branch,
                subject = subject,
                startDate = analytics.startDate.format(
                    FILE_DATE_FORMATTER
                ),
                endDate = analytics.endDate.format(
                    FILE_DATE_FORMATTER
                )
            )

            val outputFile = File(
                exportDirectory,
                fileName
            )

            writeWorkbook(
                outputFile = outputFile,
                analytics = analytics,
                semester = semester,
                branch = branch,
                subject = subject
            )

            if (
                !outputFile.exists() ||
                outputFile.length() <= 0L
            ) {
                throw IllegalStateException(
                    "The analytics workbook was not created correctly."
                )
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                outputFile
            )

            AttendanceAnalyticsExportResult(
                uri = uri,
                fileName = fileName,
                mimeType = XLSX_MIME_TYPE,
                exportedStudentCount =
                    analytics.students.size
            )
        }
    }

    private fun writeWorkbook(
        outputFile: File,
        analytics: AttendanceAnalytics,
        semester: String,
        branch: String,
        subject: String
    ) {
        XSSFWorkbook().use { workbook ->
            val styles = AnalyticsWorkbookStyles(
                workbook = workbook
            )

            createSummarySheet(
                workbook = workbook,
                analytics = analytics,
                semester = semester,
                branch = branch,
                subject = subject,
                styles = styles
            )

            createStudentsSheet(
                workbook = workbook,
                analytics = analytics,
                styles = styles
            )

            createTrendSheet(
                workbook = workbook,
                analytics = analytics,
                styles = styles
            )

            FileOutputStream(outputFile).use {
                    outputStream ->
                workbook.write(outputStream)
                outputStream.flush()
            }
        }
    }

    private fun createSummarySheet(
        workbook: XSSFWorkbook,
        analytics: AttendanceAnalytics,
        semester: String,
        branch: String,
        subject: String,
        styles: AnalyticsWorkbookStyles
    ) {
        val sheet = workbook.createSheet(
            "Summary"
        )

        var rowIndex = 0

        val titleRow = sheet.createRow(rowIndex++)
        titleRow.heightInPoints = 28f

        titleRow.createCell(0).apply {
            setCellValue(
                "Attendance Analytics Report"
            )
            cellStyle = styles.titleStyle
        }

        sheet.addMergedRegion(
            CellRangeAddress(
                0,
                0,
                0,
                SUMMARY_LAST_COLUMN
            )
        )

        rowIndex++

        createSummaryInformationRow(
            sheet = sheet,
            rowIndex = rowIndex++,
            label = "Semester",
            value = semester,
            styles = styles
        )

        createSummaryInformationRow(
            sheet = sheet,
            rowIndex = rowIndex++,
            label = "Branch",
            value = branch,
            styles = styles
        )

        createSummaryInformationRow(
            sheet = sheet,
            rowIndex = rowIndex++,
            label = "Subject",
            value = subject,
            styles = styles
        )

        createSummaryInformationRow(
            sheet = sheet,
            rowIndex = rowIndex++,
            label = "Start date",
            value = analytics.startDate.format(
                DISPLAY_DATE_FORMATTER
            ),
            styles = styles
        )

        createSummaryInformationRow(
            sheet = sheet,
            rowIndex = rowIndex++,
            label = "End date",
            value = analytics.endDate.format(
                DISPLAY_DATE_FORMATTER
            ),
            styles = styles
        )

        rowIndex++

        val metrics = listOf(
            "Attendance sessions" to
                    analytics.sessionCount.toString(),

            "Students" to
                    analytics.studentCount.toString(),

            "Class average" to
                    analytics.classAveragePercentage
                        .formatPercentage(),

            "Highest attendance" to
                    analytics.highestPercentage
                        ?.formatPercentage()
                        .orEmpty()
                        .ifBlank { "N/A" },

            "Lowest attendance" to
                    analytics.lowestPercentage
                        ?.formatPercentage()
                        .orEmpty()
                        .ifBlank { "N/A" },

            "Present entries" to
                    analytics.totalPresentEntries.toString(),

            "Absent entries" to
                    analytics.totalAbsentEntries.toString()
        )

        metrics.forEach { (label, value) ->
            createSummaryInformationRow(
                sheet = sheet,
                rowIndex = rowIndex++,
                label = label,
                value = value,
                styles = styles
            )
        }

        rowIndex++

        val riskHeaderRow =
            sheet.createRow(rowIndex++)

        listOf(
            "Risk category",
            "Student count"
        ).forEachIndexed {
                columnIndex,
                header ->

            riskHeaderRow.createCell(
                columnIndex
            ).apply {
                setCellValue(header)
                cellStyle = styles.headerStyle
            }
        }

        val distributionRows = listOf(
            "Excellent" to
                    analytics.distribution
                        .excellentCount,

            "Good" to
                    analytics.distribution
                        .goodCount,

            "Warning" to
                    analytics.distribution
                        .warningCount,

            "Critical" to
                    analytics.distribution
                        .criticalCount,

            "No data" to
                    analytics.distribution
                        .noDataCount
        )

        distributionRows.forEach {
                (label, count) ->

            val row = sheet.createRow(
                rowIndex++
            )

            row.createCell(0).apply {
                setCellValue(label)
                cellStyle = styles.bodyStyle
            }

            row.createCell(1).apply {
                setCellValue(count.toDouble())
                cellStyle =
                    styles.bodyCenteredStyle
            }
        }

        sheet.setColumnWidth(
            0,
            28 * 256
        )

        sheet.setColumnWidth(
            1,
            24 * 256
        )

        sheet.setColumnWidth(
            2,
            24 * 256
        )

        sheet.setColumnWidth(
            3,
            24 * 256
        )
    }

    private fun createStudentsSheet(
        workbook: XSSFWorkbook,
        analytics: AttendanceAnalytics,
        styles: AnalyticsWorkbookStyles
    ) {
        val sheet = workbook.createSheet(
            "Students"
        )

        val headers = listOf(
            "Sl. No.",
            "USN",
            "Student Name",
            "Present Sessions",
            "Absent Sessions",
            "Total Sessions",
            "Attendance Percentage",
            "Risk Level"
        )

        val headerRow = sheet.createRow(0)

        headers.forEachIndexed {
                columnIndex,
                header ->

            headerRow.createCell(
                columnIndex
            ).apply {
                setCellValue(header)
                cellStyle = styles.headerStyle
            }
        }

        analytics.students
            .sortedWith(
                compareBy {
                    it.percentage
                }
            )
            .forEachIndexed {
                    index,
                    student ->

                val row = sheet.createRow(
                    index + 1
                )

                row.createCell(0).apply {
                    setCellValue(
                        (index + 1).toDouble()
                    )
                    cellStyle =
                        styles.bodyCenteredStyle
                }

                row.createCell(1).apply {
                    setCellValue(student.usn)
                    cellStyle = styles.bodyStyle
                }

                row.createCell(2).apply {
                    setCellValue(
                        student.studentName
                    )
                    cellStyle = styles.bodyStyle
                }

                row.createCell(3).apply {
                    setCellValue(
                        student.presentSessions
                            .toDouble()
                    )
                    cellStyle =
                        styles.bodyCenteredStyle
                }

                row.createCell(4).apply {
                    setCellValue(
                        student.absentSessions
                            .toDouble()
                    )
                    cellStyle =
                        styles.bodyCenteredStyle
                }

                row.createCell(5).apply {
                    setCellValue(
                        student.totalSessions
                            .toDouble()
                    )
                    cellStyle =
                        styles.bodyCenteredStyle
                }

                row.createCell(6).apply {
                    setCellValue(
                        student.percentage / 100.0
                    )

                    cellStyle =
                        styles.percentageStyle
                }

                row.createCell(7).apply {
                    setCellValue(
                        student.riskLevel
                            .displayName()
                    )

                    cellStyle =
                        styles.riskStyle(
                            student.riskLevel
                        )
                }
            }

        sheet.createFreezePane(
            0,
            1
        )

        sheet.setAutoFilter(
            CellRangeAddress(
                0,
                analytics.students.size,
                0,
                headers.lastIndex
            )
        )

        sheet.setColumnWidth(
            0,
            10 * 256
        )

        sheet.setColumnWidth(
            1,
            22 * 256
        )

        sheet.setColumnWidth(
            2,
            34 * 256
        )

        sheet.setColumnWidth(
            3,
            18 * 256
        )

        sheet.setColumnWidth(
            4,
            18 * 256
        )

        sheet.setColumnWidth(
            5,
            18 * 256
        )

        sheet.setColumnWidth(
            6,
            22 * 256
        )

        sheet.setColumnWidth(
            7,
            18 * 256
        )
    }

    private fun createTrendSheet(
        workbook: XSSFWorkbook,
        analytics: AttendanceAnalytics,
        styles: AnalyticsWorkbookStyles
    ) {
        val sheet = workbook.createSheet(
            "Trend"
        )

        val headers = listOf(
            "Date",
            "Present",
            "Absent",
            "Total",
            "Attendance Percentage"
        )

        val headerRow = sheet.createRow(0)

        headers.forEachIndexed {
                columnIndex,
                header ->

            headerRow.createCell(
                columnIndex
            ).apply {
                setCellValue(header)
                cellStyle = styles.headerStyle
            }
        }

        analytics.trend.forEachIndexed {
                index,
                point ->

            val row = sheet.createRow(
                index + 1
            )

            row.createCell(0).apply {
                setCellValue(
                    point.date.format(
                        DISPLAY_DATE_FORMATTER
                    )
                )
                cellStyle = styles.bodyStyle
            }

            row.createCell(1).apply {
                setCellValue(
                    point.presentCount
                        .toDouble()
                )
                cellStyle =
                    styles.bodyCenteredStyle
            }

            row.createCell(2).apply {
                setCellValue(
                    point.absentCount
                        .toDouble()
                )
                cellStyle =
                    styles.bodyCenteredStyle
            }

            row.createCell(3).apply {
                setCellValue(
                    point.totalCount
                        .toDouble()
                )
                cellStyle =
                    styles.bodyCenteredStyle
            }

            row.createCell(4).apply {
                setCellValue(
                    point.percentage / 100.0
                )
                cellStyle =
                    styles.percentageStyle
            }
        }

        sheet.createFreezePane(
            0,
            1
        )

        sheet.setAutoFilter(
            CellRangeAddress(
                0,
                analytics.trend.size,
                0,
                headers.lastIndex
            )
        )

        sheet.setColumnWidth(
            0,
            20 * 256
        )

        sheet.setColumnWidth(
            1,
            14 * 256
        )

        sheet.setColumnWidth(
            2,
            14 * 256
        )

        sheet.setColumnWidth(
            3,
            14 * 256
        )

        sheet.setColumnWidth(
            4,
            24 * 256
        )
    }

    private fun createSummaryInformationRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        rowIndex: Int,
        label: String,
        value: String,
        styles: AnalyticsWorkbookStyles
    ) {
        val row = sheet.createRow(rowIndex)

        row.createCell(0).apply {
            setCellValue(label)
            cellStyle = styles.infoLabelStyle
        }

        row.createCell(1).apply {
            setCellValue(value)
            cellStyle = styles.infoValueStyle
        }

        sheet.addMergedRegion(
            CellRangeAddress(
                rowIndex,
                rowIndex,
                1,
                SUMMARY_LAST_COLUMN
            )
        )
    }

    private fun createFileName(
        semester: String,
        branch: String,
        subject: String,
        startDate: String,
        endDate: String
    ): String {
        return buildString {
            append("Attendance_Analytics_")
            append(semester.toSafeFilePart())
            append("_")
            append(branch.toSafeFilePart())
            append("_")
            append(subject.toSafeFilePart())
            append("_")
            append(startDate)
            append("_to_")
            append(endDate)
            append(".xlsx")
        }
    }

    private fun String.toSafeFilePart(): String {
        return trim()
            .replace(
                Regex("[^A-Za-z0-9._-]+"),
                "_"
            )
            .trim('_')
            .take(MAX_FILE_PART_LENGTH)
            .ifBlank {
                "Unknown"
            }
    }

    private fun removeExpiredExports(
        directory: File
    ) {
        val expiryTime =
            System.currentTimeMillis() -
                    EXPORT_RETENTION_MILLIS

        directory.listFiles()
            ?.filter { file ->
                file.isFile &&
                        file.lastModified() <
                        expiryTime
            }
            ?.forEach { file ->
                runCatching {
                    file.delete()
                }
            }
    }

    private fun Double.formatPercentage():
            String {
        return String.format(
            Locale.US,
            "%.1f%%",
            this
        )
    }

    private fun AttendanceRiskLevel.displayName():
            String {
        return when (this) {
            AttendanceRiskLevel.EXCELLENT ->
                "Excellent"

            AttendanceRiskLevel.GOOD ->
                "Good"

            AttendanceRiskLevel.WARNING ->
                "Warning"

            AttendanceRiskLevel.CRITICAL ->
                "Critical"

            AttendanceRiskLevel.NO_DATA ->
                "No data"
        }
    }

    private companion object {
        const val XLSX_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

        const val EXPORT_DIRECTORY =
            "exports"

        const val SUMMARY_LAST_COLUMN = 3

        const val MAX_FILE_PART_LENGTH = 40

        const val EXPORT_RETENTION_MILLIS =
            24L * 60L * 60L * 1000L

        val DISPLAY_DATE_FORMATTER:
                DateTimeFormatter =
            DateTimeFormatter.ofPattern(
                "dd MMMM yyyy"
            )

        val FILE_DATE_FORMATTER:
                DateTimeFormatter =
            DateTimeFormatter.ISO_LOCAL_DATE
    }
}
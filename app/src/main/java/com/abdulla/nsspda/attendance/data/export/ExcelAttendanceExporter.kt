package com.abdulla.nsspda.attendance.data.export

import android.content.Context
import androidx.core.content.FileProvider
import com.abdulla.nsspda.attendance.data.local.StudentAttendance
import com.abdulla.nsspda.attendance.domain.model.export.AttendanceExportResult
import com.abdulla.nsspda.attendance.domain.model.export.AttendanceExporter
import com.abdulla.nsspda.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExcelAttendanceExporter @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : AttendanceExporter {

    override suspend fun exportAttendance(
        attendance: List<StudentAttendance>,
        semester: String,
        branch: String,
        subject: String,
        date: LocalDate
    ): AttendanceExportResult {
        return withContext(ioDispatcher) {
            require(attendance.isNotEmpty()) {
                "There are no attendance records to export."
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

            removeExpiredExports()

            val exportDirectory = File(
                context.cacheDir,
                EXPORT_DIRECTORY
            ).apply {
                if (!exists() && !mkdirs()) {
                    throw IllegalStateException(
                        "Unable to create the export directory."
                    )
                }
            }

            val fileName = createFileName(
                semester = semester,
                branch = branch,
                subject = subject,
                date = date
            )

            val outputFile = File(
                exportDirectory,
                fileName
            )

            writeWorkbook(
                outputFile = outputFile,
                attendance = attendance,
                semester = semester,
                branch = branch,
                subject = subject,
                date = date
            )

            if (
                !outputFile.exists() ||
                outputFile.length() <= 0L
            ) {
                throw IllegalStateException(
                    "The exported workbook was not created correctly."
                )
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                outputFile
            )

            AttendanceExportResult(
                uri = uri,
                fileName = fileName,
                mimeType = XLSX_MIME_TYPE,
                exportedRowCount = attendance.size
            )
        }
    }

    private fun writeWorkbook(
        outputFile: File,
        attendance: List<StudentAttendance>,
        semester: String,
        branch: String,
        subject: String,
        date: LocalDate
    ) {
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet(
                "Attendance"
            )

            val styles = AttendanceWorkbookStyles(
                workbook = workbook
            )

            var rowIndex = 0

            val titleRow = sheet.createRow(rowIndex++)
            titleRow.heightInPoints = 28f

            titleRow.createCell(0).apply {
                setCellValue("Attendance Report")
                cellStyle = styles.titleStyle
            }

            sheet.addMergedRegion(
                org.apache.poi.ss.util.CellRangeAddress(
                    0,
                    0,
                    0,
                    LAST_COLUMN_INDEX
                )
            )

            rowIndex++

            createInformationRow(
                sheet = sheet,
                rowIndex = rowIndex++,
                label = "Semester",
                value = semester,
                styles = styles
            )

            createInformationRow(
                sheet = sheet,
                rowIndex = rowIndex++,
                label = "Branch",
                value = branch,
                styles = styles
            )

            createInformationRow(
                sheet = sheet,
                rowIndex = rowIndex++,
                label = "Subject",
                value = subject,
                styles = styles
            )

            createInformationRow(
                sheet = sheet,
                rowIndex = rowIndex++,
                label = "Date",
                value = date.format(
                    DISPLAY_DATE_FORMATTER
                ),
                styles = styles
            )

            createInformationRow(
                sheet = sheet,
                rowIndex = rowIndex++,
                label = "Total students",
                value = attendance.size.toString(),
                styles = styles
            )

            createInformationRow(
                sheet = sheet,
                rowIndex = rowIndex++,
                label = "Present",
                value = attendance.count {
                    it.present
                }.toString(),
                styles = styles
            )

            createInformationRow(
                sheet = sheet,
                rowIndex = rowIndex++,
                label = "Absent",
                value = attendance.count {
                    !it.present
                }.toString(),
                styles = styles
            )

            rowIndex++

            val headerRow = sheet.createRow(
                rowIndex++
            )

            val headers = listOf(
                "Sl. No.",
                "USN",
                "Student Name",
                "Status"
            )

            headers.forEachIndexed {
                    columnIndex,
                    header ->

                headerRow.createCell(
                    columnIndex
                ).apply {
                    setCellValue(header)
                    cellStyle =
                        styles.headerStyle
                }
            }

            attendance
                .sortedBy {
                    it.usn.trim().uppercase()
                }
                .forEachIndexed {
                        index,
                        record ->

                    val row = sheet.createRow(
                        rowIndex++
                    )

                    row.createCell(0).apply {
                        setCellValue(
                            (index + 1).toDouble()
                        )
                        cellStyle =
                            styles.bodyCenteredStyle
                    }

                    row.createCell(1).apply {
                        setCellValue(record.usn)
                        cellStyle =
                            styles.bodyStyle
                    }

                    row.createCell(2).apply {
                        setCellValue(
                            record.studentName
                        )
                        cellStyle =
                            styles.bodyStyle
                    }

                    row.createCell(3).apply {
                        setCellValue(
                            if (record.present) {
                                "Present"
                            } else {
                                "Absent"
                            }
                        )

                        cellStyle =
                            if (record.present) {
                                styles.presentStyle
                            } else {
                                styles.absentStyle
                            }
                    }
                }

            sheet.createFreezePane(
                0,
                HEADER_FREEZE_ROW
            )

            sheet.setAutoFilter(
                org.apache.poi.ss.util.CellRangeAddress(
                    HEADER_FREEZE_ROW - 1,
                    rowIndex - 1,
                    0,
                    LAST_COLUMN_INDEX
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
                35 * 256
            )

            sheet.setColumnWidth(
                3,
                16 * 256
            )

            FileOutputStream(
                outputFile
            ).use { outputStream ->
                workbook.write(
                    outputStream
                )

                outputStream.flush()
            }
        }
    }

    private fun createInformationRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        rowIndex: Int,
        label: String,
        value: String,
        styles: AttendanceWorkbookStyles
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
            org.apache.poi.ss.util.CellRangeAddress(
                rowIndex,
                rowIndex,
                1,
                LAST_COLUMN_INDEX
            )
        )
    }

    private fun createFileName(
        semester: String,
        branch: String,
        subject: String,
        date: LocalDate
    ): String {
        val safeSemester =
            semester.toSafeFilePart()

        val safeBranch =
            branch.toSafeFilePart()

        val safeSubject =
            subject.toSafeFilePart()

        val datePart = date.format(
            FILE_DATE_FORMATTER
        )

        return "Attendance_" +
                "${safeSemester}_" +
                "${safeBranch}_" +
                "${safeSubject}_" +
                "$datePart.xlsx"
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

    private fun removeExpiredExports() {
        val directory = File(
            context.cacheDir,
            EXPORT_DIRECTORY
        )

        if (!directory.exists()) {
            return
        }

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

    private companion object {
        const val XLSX_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

        const val EXPORT_DIRECTORY =
            "exports"

        const val LAST_COLUMN_INDEX = 3

        /*
         * Row 10 contains the table headers because:
         * title = 0
         * spacer = 1
         * information = 2 through 8
         * spacer = 9
         * table header = 10
         */
        const val HEADER_FREEZE_ROW = 11

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
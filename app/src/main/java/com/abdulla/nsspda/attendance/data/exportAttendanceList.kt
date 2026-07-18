package com.abdulla.nsspda.attendance.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun exportAttendanceList(
    context: Context,
    attendanceList: List<StudentAttendance>,
    semester: String,
    branch: String,
    subject: String,
    date: String
) {
    try {

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Attendance")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("USN")
        headerRow.createCell(2).setCellValue("Present")

        for ((index, attendance) in attendanceList.withIndex()) {
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(attendance.studentName)
            row.createCell(1).setCellValue(attendance.usn)
            row.createCell(2).setCellValue(if (attendance.present) "Present" else "Absent")
        }


        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, "Attendance_$semester-$branch-$subject-$date.xlsx")


        val fileOut = FileOutputStream(file)
        workbook.write(fileOut)
        fileOut.close()

        workbook.close()

        shareFile(context, file)

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error exporting file", Toast.LENGTH_SHORT).show()
    }
}

private fun shareFile(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Attendance File"))

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error sharing file", Toast.LENGTH_SHORT).show()
    }
}


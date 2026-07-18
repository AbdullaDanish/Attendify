package com.abdulla.nsspda.student.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.abdulla.nsspda.student.viewmodel.StudentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream


fun importFile(
    context: Context,
    uri: Uri,
    nameHeader: String,
    usnHeader: String,
    semester: String,
    branch: String,
    subject: String,
    viewModel: StudentViewModel,
    showLoading: (Boolean) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        showLoading(true)
        try {
            withContext(Dispatchers.IO) {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error opening file", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                var nameColumnIndex = -1
                var usnColumnIndex = -1
                var nameRowIndex = -1
                var usnRowIndex = -1

                // Normalize input headers
                val normalizedNameHeader = nameHeader.trim().lowercase()
                val normalizedUsnHeader = usnHeader.trim().lowercase()

                // Search headers in the sheet
                outerLoop@ for (rowIndex in 0 until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    for (cellIndex in 0 until row.physicalNumberOfCells) {
                        val cellValue = row.getCell(cellIndex)?.toString()?.trim()?.lowercase()

                        if (cellValue == normalizedNameHeader && nameColumnIndex == -1) {
                            nameColumnIndex = cellIndex
                            nameRowIndex = rowIndex
                        }
                        if (cellValue == normalizedUsnHeader && usnColumnIndex == -1) {
                            usnColumnIndex = cellIndex
                            usnRowIndex = rowIndex
                        }

                        if (nameColumnIndex != -1 && usnColumnIndex != -1) {
                            break@outerLoop
                        }
                    }
                }

                if (nameColumnIndex == -1 || usnColumnIndex == -1) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Couldn't find headers: \"$nameHeader\", \"$usnHeader\"", Toast.LENGTH_LONG).show()
                    }
                    workbook.close()
                    inputStream.close()
                    return@withContext
                }

                val startRowIndex = maxOf(nameRowIndex, usnRowIndex) + 1
                val students = mutableListOf<Student>()

                for (rowIndex in startRowIndex until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    val studentName = row.getCell(nameColumnIndex)?.toString()?.trim()
                    val usn = row.getCell(usnColumnIndex)?.toString()?.trim()

                    if (!studentName.isNullOrEmpty() && !usn.isNullOrEmpty()) {
                        Log.d("ExcelRow", "Row $rowIndex - Name: $studentName, USN: $usn")
                        students.add(
                            Student(
                                studentName = studentName.uppercase(),
                                usn = usn.uppercase(),
                                semester = semester,
                                branch = branch,
                                subject = subject
                            )
                        )
                    }
                }

                students.forEach { student ->
                    viewModel.addStudent(student, semester, branch)
                }

                workbook.close()
                inputStream.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Imported ${students.size} students", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error importing file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            showLoading(false)
        }
    }
}










package com.abdulla.nsspda.attendance.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abdulla.nsspda.R
import com.abdulla.nsspda.attendance.viewmodel.AttendancePercentage
import com.abdulla.nsspda.attendance.viewmodel.AttendanceViewmodel
import com.abdulla.nsspda.student.presentation.DatePickerDialog
import com.abdulla.nsspda.student.presentation.toDate
import kotlinx.coroutines.delay
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalculatePercentageScreen(
    semester: String?,
    branch: String?,
    subject: String?,
    navController: NavController
) {
    val viewModel: AttendanceViewmodel = viewModel()
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    val attendancePercentages by viewModel.attendancePercentages.observeAsState(emptyList())
    var isLoading by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var triggerExport by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculate Percentage", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onPrimary)
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                actions = {
                    Button(
                        onClick = {
                            if (attendancePercentages.isNotEmpty()) {
                                triggerExport = true
                            } else {
                                Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Export",
                            tint = MaterialTheme.colors.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export", color = MaterialTheme.colors.onPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.pdalogo),
                contentDescription = "PDA Logo Watermark",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.06f),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Date pickers for Start and End Date
                DateOutlineAndPick("Start Date") { selectedStartDate ->
                    startDate = selectedStartDate
                }
                Spacer(modifier = Modifier.height(8.dp))
                DateOutlineAndPick("End Date") { selectedEndDate ->
                    endDate = selectedEndDate
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isLoading = true
                        viewModel.calculateAttendancePercentage(
                            semester.orEmpty(), branch.orEmpty(), subject.orEmpty(),
                            startDate.toDate(), endDate.toDate()
                        )
                    }
                ) {
                    Text("Calculate Percentage")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()

                    LaunchedEffect(attendancePercentages) {
                        delay(500)
                        isLoading = false
                    }
                } else {
                    if (attendancePercentages.isEmpty()) {
                        Text("No data available", style = MaterialTheme.typography.body1)
                    } else {
                        AttendanceList(attendancePercentages)
                    }
                }

            }
        }
        if (showLoadingDialog) {
            AlertDialog(
                onDismissRequest = { /* Disable dismiss */ },
                title = { Text("Exporting...") },
                text = { CircularProgressIndicator() },
                buttons = {}
            )
        }
        if (triggerExport) {
            LaunchedEffect(Unit) {
                showLoadingDialog = true
                delay(500)
                showLoadingDialog = false
                triggerExport = false

                val exportedFile = exportToXLS(context, attendancePercentages, semester.orEmpty(), subject.orEmpty())

                exportedFile?.let {
                    shareFile(context, it)
                } ?: run {
                    Toast.makeText(context, "Failed to export or file is empty", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}

@Composable
fun AttendanceList(attendancePercentages: List<AttendancePercentage>) {
    LazyColumn {
        items(attendancePercentages) { percentage ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Name: ${percentage.studentName}",
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onSurface,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "USN: ${percentage.usn}",
                            style = MaterialTheme.typography.body2.copy(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                fontSize = 16.sp
                            )
                        )
                    }

                    Text(
                        text = "${percentage.percentage}%",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 18.sp
                        )
                    )
                }
                Divider(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DateOutlineAndPick(label: String, onDateSelected: (LocalDate) -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val formattedSelectedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = formattedSelectedDate,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .weight(1f)
                .clickable { showDatePicker = true }
                .padding(end = 8.dp)
        )

        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = "Pick Date")
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { newDate: LocalDate ->
                selectedDate = newDate
                onDateSelected(newDate)
                showDatePicker = false
            },
            initialDate = selectedDate
        )
    }
}

fun exportToXLS(context: Context, attendancePercentages: List<AttendancePercentage>, semester: String, subject: String): File? {
    val fileName = "Attendance_Percentage_Report_${semester}_${subject}.xls"
    val file = File(context.cacheDir, fileName)


    try {

        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Attendance")


        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("USN")
        headerRow.createCell(1).setCellValue("Student Name")
        headerRow.createCell(2).setCellValue("Percentage")


        attendancePercentages.forEachIndexed { index, attendance ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(attendance.usn)
            row.createCell(1).setCellValue(attendance.studentName)
            row.createCell(2).setCellValue(attendance.percentage)
        }


        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return if (file.exists()) file else null
}




fun shareFile(context: Context, file: File) {
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






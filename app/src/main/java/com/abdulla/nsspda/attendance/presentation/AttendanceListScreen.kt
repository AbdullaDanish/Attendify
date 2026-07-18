package com.abdulla.nsspda.attendance.presentation

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abdulla.nsspda.attendance.data.StudentAttendance
import com.abdulla.nsspda.attendance.data.exportAttendanceList
import com.abdulla.nsspda.attendance.viewmodel.AttendanceViewmodel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("InvalidColorHexValue")
@Composable
fun AttendanceListScreen(
    semester: String?,
    branch: String?,
    date: Date?,
    subject: String?,
    navController: NavController
) {
    val studentAttendanceViewModel: AttendanceViewmodel = viewModel()
    val attendanceList by studentAttendanceViewModel.attendanceList.observeAsState(emptyList())
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val formattedDate = remember(date) { date?.let { dateFormat.format(it) } ?: "No Date" }
    var isEditMode by rememberSaveable { mutableStateOf(false) }
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    val updatedAttendanceList = remember { mutableStateListOf<StudentAttendance>() }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var showLoadingDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(semester, branch, subject, date) {
        if (semester != null && branch != null && date != null && subject != null) {
            delay(500)
            studentAttendanceViewModel.fetchAttendanceBySemesterBranchDate(
                semester, branch, subject, date
            )
            isLoading = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Attendance Record: $formattedDate", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onPrimary)
                    }
                },
                actions = {
                    if (isEditMode) {
                        Button(
                            onClick = {
                                studentAttendanceViewModel.updateAttendanceList(updatedAttendanceList)
                                isEditMode = false
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Attendance updated successfully")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                        ) {
                            Text("Update")
                        }
                    } else {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = MaterialTheme.colors.onPrimary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                if (semester != null && branch != null && date != null) {
                                    showConfirmationDialog = true
                                }
                                showMenu = false
                            }) {
                                Text("Delete List")
                            }
                            DropdownMenuItem(onClick = {
                                if (attendanceList.isNotEmpty()) {
                                    showLoadingDialog = true
                                    coroutineScope.launch {
                                        exportAttendanceList(context, attendanceList, semester ?: "", branch ?: "", subject ?: "", formattedDate)
                                        delay(500)
                                        showLoadingDialog = false
                                        Toast.makeText(context, "Exported Successfully", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                                }
                                showMenu = false
                            }) {
                                Text("Export List")
                            }
                            DropdownMenuItem(onClick = {
                                isEditMode = true
                                updatedAttendanceList.clear()
                                updatedAttendanceList.addAll(attendanceList)
                                showMenu = false
                            }) {
                                Text("Update Attendance")
                            }
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(attendanceList, key = { it.usn }) { attendance ->
                    AttendanceRow(attendance, isEditMode, updatedAttendanceList)
                }
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this list?") },
            confirmButton = {
                Button(onClick = {
                    studentAttendanceViewModel.deleteAttendanceBySemesterBranchDate(
                        semester ?: return@Button,
                        branch ?: return@Button,
                        subject ?: return@Button,
                        date ?: return@Button
                    )
                    Toast.makeText(context, "List deleted", Toast.LENGTH_SHORT).show()
                    showConfirmationDialog = false
                    navController.navigateUp() // Navigate back after deletion
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showLoadingDialog) {
        AlertDialog(
            onDismissRequest = { /* Disable dismiss */ },
            title = { Text("Exporting...") },
            text = { CircularProgressIndicator() },
            buttons = {}
        )
    }
}

@Composable
fun AttendanceRow(
    attendance: StudentAttendance,
    isEditMode: Boolean,
    updatedAttendanceList: SnapshotStateList<StudentAttendance>
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
                text = "Name: ${attendance.studentName}",
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "USN: ${attendance.usn}",
                style = MaterialTheme.typography.body2.copy(fontSize = 16.sp)
            )
        }

        if (isEditMode) {
            Checkbox(
                checked = updatedAttendanceList.find { it.usn == attendance.usn }?.present ?: attendance.present,
                onCheckedChange = { updatedPresent ->
                    val index = updatedAttendanceList.indexOfFirst { it.usn == attendance.usn }
                    if (index != -1) {
                        updatedAttendanceList[index] = updatedAttendanceList[index].copy(present = updatedPresent)
                    }
                }
            )
        } else {
            Text(
                text = if (attendance.present) "P" else "A",
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                color = if (attendance.present) Color(0xFF5EA838) else Color(0xFFEE4949) // Green for Present, Red for Absent
            )
        }
    }
    Divider(
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
        thickness = 1.dp,
        modifier = Modifier.padding(top = 8.dp)
    )
}





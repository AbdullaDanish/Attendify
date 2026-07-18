package com.abdulla.nsspda.student.presentation

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abdulla.nsspda.R
import com.abdulla.nsspda.attendance.data.StudentAttendance
import com.abdulla.nsspda.attendance.viewmodel.AttendanceViewmodel
import com.abdulla.nsspda.student.data.Student
import com.abdulla.nsspda.student.data.importFile
import com.abdulla.nsspda.student.viewmodel.StudentViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date


fun LocalDate.toDate(): Date {
    return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun StudentScreen(semester: String?, branch: String?,
                  subject: String?,
                  navController: NavController) {
    val studentViewModel: StudentViewModel = viewModel()
    val attendanceViewModel: AttendanceViewmodel = viewModel()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    val studentAttendanceMap = remember { mutableStateMapOf<String, Boolean>() }
    val formattedDate = selectedDate.toDate()
    val students by studentViewModel.students.observeAsState(emptyList())
    var showConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val formattedSelectedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    var showImportFileDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    var nameHeader by remember { mutableStateOf("") }
    var usnHeader  by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            importFile(
                context      = context,
                uri          = it,
                nameHeader   = nameHeader,
                usnHeader    = usnHeader,
                semester     = semester.orEmpty(),
                branch       = branch.orEmpty(),
                subject      = subject.orEmpty(),
                viewModel    = studentViewModel
            ) { loading ->
                isLoading = loading
            }
        }
    }
    var isToggled by remember { mutableStateOf(false) }
    val insertSubject = subject.toString()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mark Attendance",
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showAddStudentDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Student",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(onClick = {
                            showDeleteConfirmDialog = true
                            showMenu = false
                        }) {
                            Text("Delete All Students")
                        }
                        DropdownMenuItem(onClick = {
                            showMapDialog = true
                            showMenu = false
                        }) {
                            Text("Import File")
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showConfirmDialog = true
                },
                backgroundColor = MaterialTheme.colors.secondary,
                contentColor = MaterialTheme.colors.onSecondary,
                shape = RoundedCornerShape(10),
                modifier = Modifier.size(85.dp)
            ) {
                Text(
                    text = "Save",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 16.sp
                )
            }
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
                    .alpha(0.03f),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Sem: $semester",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp,
                        color = MaterialTheme.colors.primary
                    )

                    Text(
                        "Sub: $insertSubject",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp,
                        color = MaterialTheme.colors.primary
                    )

                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = formattedSelectedDate,
                        onValueChange = {},
                        label = { Text("Select Date") },
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
                            .align(Alignment.CenterVertically),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text(text = "Pick Date")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Enable Reverse Attendance Marking",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Switch(
                        checked = isToggled,
                        onCheckedChange = { isToggled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary)
                    )
                }


                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(students) { student ->
                        StudentItem(
                            student = student,
                            isChecked = studentAttendanceMap[student.usn] ?: false,
                            onCheckedChange = { isChecked ->
                                studentAttendanceMap[student.usn] = isChecked
                            },
                            onLongPressDelete = { student ->
                                studentViewModel.deleteStudent(student)
                                Toast.makeText(
                                    context,
                                    "Student deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        Divider()

                    }

                }
            }
            LaunchedEffect(Unit) {
                studentViewModel.getStudentsBySemesterAndBranch(
                    semester.toString(),
                    branch.toString()
                )
            }

            if (showDatePicker) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = { showDatePicker = false })
                ) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        onDateSelected = { newDate ->
                            selectedDate = newDate
                            showDatePicker = false
                        },
                        initialDate = selectedDate
                    )
                }
            }

            if (showAddStudentDialog) {
                if (semester != null) {
                    if (branch != null) {
                        if (subject != null)
                            AddStudentDialog(
                                onDismissRequest = { showAddStudentDialog = false },
                                onAddStudent = { student ->
                                    studentViewModel.addStudent(student, semester, branch)
                                    showAddStudentDialog = false
                                },
                                semesters = semester,
                                branches = branch,
                                subject = subject
                            )
                    }
                }
            }
            if (showConfirmDialog) {
                ConfirmationDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    onConfirm = {
                        students.forEach { student ->
                            val isPresent =
                                if (isToggled) !(studentAttendanceMap[student.usn] ?: false)
                                else (studentAttendanceMap[student.usn] ?: false)
                            attendanceViewModel.addAttendance(
                                StudentAttendance(
                                    studentName = student.studentName,
                                    usn = student.usn,
                                    semester = student.semester,
                                    branch = student.branch,
                                    subject = insertSubject,
                                    present = isPresent,
                                    date = formattedDate
                                )
                            )
                        }

                        studentAttendanceMap.clear()
                        navController.navigate("detailsScreen/${semester}/${branch}/${subject}") {
                            popUpTo("detailsScreen2/${semester}/${branch}/${subject}") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                        Toast.makeText(context, "Attendance saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            }
            if (showImportFileDialog) {
                showImportFileDialog = false
                launcher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            }
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Delete All Students") },
                    text = { Text("Are you sure you want to delete all students for $semester $branch? This action cannot be undone.") },
                    confirmButton = {
                        Button(onClick = {
                            studentViewModel.deleteAllStudents(
                                semester.toString(),
                                branch.toString()
                            )
                            showDeleteConfirmDialog = false
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            if (isLoading) {
                LoadingDialog(show = isLoading)
            }
            if (showMapDialog) {
                ImportHeaderMappingDialog(
                    onDismiss = { showMapDialog = false },
                    onConfirm = { nameH, usnH ->
                        nameHeader = nameH
                        usnHeader  = usnH
                        showMapDialog = false
                        launcher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    }
                )
            }

        }
    }
}

@Composable
fun LoadingDialog(show: Boolean) {
    if (show) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Loading", fontSize = 18.sp)
            },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Importing data, please wait...", fontSize = 16.sp)
                }
            },
            buttons = {}
        )
    }
}


@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Confirm Submission") },
        text = { Text("Are you sure you want to submit the attendance? please recheck the date and submit") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismissRequest()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        time = Date.from(initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(newDate)
                onDismissRequest()
            },
            year,
            month,
            day
        ).apply {
            setOnDismissListener { onDismissRequest() }
        }
    }

    LaunchedEffect(datePickerDialog) {
        datePickerDialog.show()
    }

    DisposableEffect(Unit) {
        onDispose {
            datePickerDialog.dismiss()
        }
    }
}
@Composable
fun AddStudentDialog(
    onDismissRequest: () -> Unit,
    onAddStudent: (Student) -> Unit,
    semesters: String,
    branches: String,
    subject: String
) {
    var studentName by remember { mutableStateOf("") }
    var usn by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add Student") },
        text = {
            Column {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it.uppercase() },
                    label = { Text("Student Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = usn,
                    onValueChange = { usn = it.uppercase() },
                    label = { Text("USN") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val student = Student(
                        studentName = studentName,
                        usn = usn,
                        semester = semesters,
                        branch = branches,
                        subject = subject
                    )
                    onAddStudent(student)
                    onDismissRequest()
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun StudentItem(
    student: Student,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    onLongPressDelete: (Student) -> Unit = {}
) {
    var showDeleteMenu by remember { mutableStateOf(false) }
    var menuPosition by remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        menuPosition = offset
                        showDeleteMenu = true
                    }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = student.studentName, style = MaterialTheme.typography.body1)
                Text(text = student.usn, style = MaterialTheme.typography.body2, color = Color.DarkGray)
            }
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }


        if (showDeleteMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showDeleteMenu = false }
            ) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(menuPosition.x.toInt(), menuPosition.y.toInt())
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .border(1.dp, Color.Gray)
                            .padding(8.dp)
                    ) {
                        Text("Delete Student", modifier = Modifier.clickable {
                            onLongPressDelete(student)
                            showDeleteMenu = false
                        })
                    }
                }
            }
        }
    }
}
@Composable
fun ImportHeaderMappingDialog(
    onDismiss: () -> Unit,
    onConfirm: (nameHeader: String, usnHeader: String) -> Unit
) {
    var nameHeader by remember { mutableStateOf("") }
    var usnHeader by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Map Excel Headers") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameHeader,
                    onValueChange = { nameHeader = it },
                    label = { Text("Student Name Header") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = usnHeader,
                    onValueChange = { usnHeader = it },
                    label = { Text("Student USN Header") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameHeader.isNotBlank() && usnHeader.isNotBlank()) {
                        onConfirm(nameHeader.trim(), usnHeader.trim())
                    }
                }
            ) {
                Text("Select File")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}










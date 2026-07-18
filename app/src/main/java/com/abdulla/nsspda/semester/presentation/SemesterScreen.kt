package com.abdulla.nsspda.semester.presentation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.abdulla.nsspda.R
import com.abdulla.nsspda.semester.data.Semester
import com.abdulla.nsspda.semester.viewmodel.SemesterViewModel
import com.abdulla.nsspda.ui.theme.NSSPDATheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SemesterListScreen(navController: NavController) {
    val viewModel : SemesterViewModel = viewModel()
    val semestersList by viewModel.semesterList.observeAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Pair<Semester?, Boolean>>(null to false) }
    val context = LocalContext.current
    val activity = remember { context as? Activity }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {

            TopAppBar(
                title = { Text("ATTENDIFY",
                    fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                            activity?.finish()

                    }) {
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onPrimary)
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true },
                    shape = RoundedCornerShape(10)) {
                Text(text = "Add Class",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 16.sp)
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
                    .alpha(0.1f),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            ) {
                if (showDialog) {
                    AddSemesterDialog(viewModel = viewModel, onDismiss = { showDialog = false })
                }

                semestersList?.let {
                    LazyColumn(
                           modifier = Modifier
                               .padding(bottom = 100.dp)
                               .fillMaxSize()
                    ) {
                        items(it) { semester ->
                            SemesterList(semester,
                                onDelete = { selectedSemester ->
                                    showDeleteDialog = selectedSemester to true
                                },
                                onClick = {
                                    navController.navigate("detailsScreen/${semester.semester}/${semester.branch}/${semester.subject}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    showDeleteDialog.first?.let { semesterToDelete ->
        if (showDeleteDialog.second) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null to false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete this semester?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSemester(semesterToDelete)
                            showDeleteDialog = null to false
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = null to false }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}
@Composable
fun EditableDropdownMenuWithLabel(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var newOption by rememberSaveable { mutableStateOf("") }

    Column {
        Text(text = label, style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .border(
                    1.dp,
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                    RoundedCornerShape(4.dp)
                )
                .background(MaterialTheme.colors.surface, RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Text(
                text = selectedOption.ifEmpty { "Select an option"},
                style = MaterialTheme.typography.body2,
                color = if (selectedOption.isEmpty()) MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                else Color.Black            )
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                        RoundedCornerShape(4.dp)
                    )
                    .heightIn(max = 200.dp)
            ) {
                LazyColumn {
                    items(options) { option ->
                        DropdownMenuItem(onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }) {
                            Text(text = option, color = Color.Black)                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newOption,
                                onValueChange = { newOption = it.uppercase() },
                                label = { Text("Didn’t find an option? Enter manually", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = VisualTransformation.None,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = Color.Black
                                )

                            )


                            if (newOption.isEmpty()) {
                                Text(
                                    text = "You can enter a custom option if it's not listed.",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Button(
                                onClick = {
                                    if (newOption.isNotEmpty()) {
                                        onOptionSelected(newOption)
                                        newOption = ""
                                        expanded = false
                                    }
                                },
                                enabled = newOption.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF6200EE),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Select")
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun AddSemesterDialog(viewModel: SemesterViewModel, onDismiss: () -> Unit) {
    var selectedSemester by rememberSaveable { mutableStateOf("") }
    var selectedBranch by rememberSaveable { mutableStateOf("") }
    var selectedSubject by rememberSaveable { mutableStateOf("") }

    val semesters = remember { listOf("3A", "3B", "4A", "4B", "5A", "5B", "6A", "6B", "7A", "7B", "8A", "8B") }
    val branches = remember { listOf("CSE", "ECE", "ME", "CSD", "AIML") }
    val subjects = remember { listOf("NSS", "PE", "Yoga") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Fill Details") },
        text = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime)
                .verticalScroll(rememberScrollState())
                .heightIn(min = 100.dp, max = 400.dp)
            ) {
                EditableDropdownMenuWithLabel(
                    label = "Select Semester and Section",
                    options = semesters,
                    selectedOption = selectedSemester,
                    onOptionSelected = { selectedSemester = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditableDropdownMenuWithLabel(
                    label = "Select Branch",
                    options = branches,
                    selectedOption = selectedBranch,
                    onOptionSelected = { selectedBranch = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditableDropdownMenuWithLabel(
                    label = "Select Subject",
                    options = subjects,
                    selectedOption = selectedSubject,
                    onOptionSelected = { selectedSubject = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedSemester.isNotEmpty() && selectedBranch.isNotEmpty() && selectedSubject.isNotEmpty()) {
                    val newSemester = Semester(
                        semester = selectedSemester,
                        branch = selectedBranch,
                        subject = selectedSubject
                    )
                    viewModel.addSemester(newSemester)

                    onDismiss()
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

//@Composable
//fun DropdownMenuWithLabel(
//    label: String,
//    options: List<String>,
//    selectedOption: String,
//    onOptionSelected: (String) -> Unit
//) {
//    var expanded by rememberSaveable { mutableStateOf(false) }
//
//    Column {
//        Text(text = label, style = MaterialTheme.typography.body1)
//        Spacer(modifier = Modifier.height(8.dp))
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { expanded = true }
//                .border(
//                    1.dp,
//                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
//                    RoundedCornerShape(4.dp)
//                )
//                .background(MaterialTheme.colors.surface, RoundedCornerShape(4.dp))
//                .padding(16.dp)
//        ) {
//            Text(
//                text = if (selectedOption.isEmpty()) "Select an option" else selectedOption,
//                style = MaterialTheme.typography.body2,
//                color = if (selectedOption.isEmpty()) MaterialTheme.colors.onSurface.copy(alpha = 0.6f) else MaterialTheme.colors.onSurface
//            )
//        }
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            options.forEach { option ->
//                DropdownMenuItem(onClick = {
//                    onOptionSelected(option)
//                    expanded = false
//                }) {
//                    Text(text = option)
//                }
//            }
//        }
//    }
//}

@Composable
fun SemesterList(
    semester: Semester,
    onDelete: (Semester) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colors.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Semester: ${semester.semester[0]}",
                    style = MaterialTheme.typography.h5.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary,
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Subject: ${semester.subject}",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Branch: ${semester.branch}",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Section: ${semester.semester.substring(1)}",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                )
            }

            IconButton(
                onClick = { onDelete(semester) },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colors.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun screenPreview(){
    NSSPDATheme {
        SemesterListScreen(navController = rememberNavController())
    }
}




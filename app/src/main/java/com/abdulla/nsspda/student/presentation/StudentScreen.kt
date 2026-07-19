package com.abdulla.nsspda.student.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.R
import com.abdulla.nsspda.student.presentation.components.AddStudentDialog
import com.abdulla.nsspda.student.presentation.components.ClassInformationCard
import com.abdulla.nsspda.student.presentation.components.DeleteAllStudentsDialog
import com.abdulla.nsspda.student.presentation.components.DeleteStudentDialog
import com.abdulla.nsspda.student.presentation.components.EmptyStudentContent
import com.abdulla.nsspda.student.presentation.components.ErrorContent
import com.abdulla.nsspda.student.presentation.components.LoadingContent
import com.abdulla.nsspda.student.presentation.components.StudentList
import com.abdulla.nsspda.student.presentation.imports.ImportErrorDialog
import com.abdulla.nsspda.student.presentation.imports.ImportMappingDialog
import com.abdulla.nsspda.student.presentation.imports.ImportPreviewDialog
import com.abdulla.nsspda.student.presentation.imports.ImportProgressDialog
import com.abdulla.nsspda.student.presentation.imports.ImportResultDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    uiState: StudentUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (StudentIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
)  {
    var isOverflowMenuVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Students",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (uiState.subject.isNotBlank()) {
                            Text(
                                text = uiState.subject,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = uiState.isClassValid &&
                                !uiState.isSubmitting,
                        onClick = {
                            onIntent(
                                StudentIntent.AddStudentClicked
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add student"
                        )
                    }

                    Box {
                        IconButton(
                            enabled = !uiState.isSubmitting,
                            onClick = {
                                isOverflowMenuVisible = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = isOverflowMenuVisible,
                            onDismissRequest = {
                                isOverflowMenuVisible = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Import from Excel")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            Icons.Default.UploadFile,
                                        contentDescription = null
                                    )
                                },
                                enabled =
                                    uiState.isClassValid &&
                                            !uiState.isImportInProgress,
                                onClick = {
                                    isOverflowMenuVisible = false

                                    onIntent(
                                        StudentIntent
                                            .ImportStudentsClicked
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Delete all students")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            Icons.Default.DeleteSweep,
                                        contentDescription = null
                                    )
                                },
                                enabled =
                                    uiState.students.isNotEmpty() &&
                                            !uiState.isSubmitting,
                                onClick = {
                                    isOverflowMenuVisible = false

                                    onIntent(
                                        StudentIntent.DeleteAllClicked
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults
                    .centerAlignedTopAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
            )
        },
        floatingActionButton = {
            val isAddStudentEnabled =
                uiState.isClassValid &&
                        !uiState.isSubmitting

            ExtendedFloatingActionButton(
                onClick = {
                    if (isAddStudentEnabled) {
                        onIntent(
                            StudentIntent.AddStudentClicked
                        )
                    }
                },
                containerColor = if (isAddStudentEnabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isAddStudentEnabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.38f)
                },
                modifier = Modifier.alpha(
                    if (isAddStudentEnabled) 1f else 0.6f
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text("Add student")
                }
            )        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.pdalogo
                ),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.035f),
                contentScale = ContentScale.Fit
            )

            StudentScreenContent(
                uiState = uiState,
                onIntent = onIntent,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (uiState.isAddDialogVisible) {
        AddStudentDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = {
                onIntent(
                    StudentIntent.AddStudentDismissed
                )
            },
            onSubmit = { name, usn ->
                onIntent(
                    StudentIntent.AddStudentSubmitted(
                        studentName = name,
                        usn = usn
                    )
                )
            }
        )
    }

    uiState.studentToDelete?.let { student ->
        DeleteStudentDialog(
            student = student,
            isSubmitting = uiState.isSubmitting,
            onConfirm = {
                onIntent(
                    StudentIntent.DeleteStudentConfirmed
                )
            },
            onDismiss = {
                onIntent(
                    StudentIntent.DeleteStudentDismissed
                )
            }
        )
    }

    if (uiState.isDeleteAllDialogVisible) {
        DeleteAllStudentsDialog(
            uiState = uiState,
            onConfirm = {
                onIntent(
                    StudentIntent.DeleteAllConfirmed
                )
            },
            onDismiss = {
                onIntent(
                    StudentIntent.DeleteAllDismissed
                )
            }
        )
    }

    when (
        val importState =
            uiState.importState
    ) {
        StudentImportUiState.Idle,
        StudentImportUiState
            .RequestFileSelection -> Unit

        is StudentImportUiState.Analyzing -> {
            ImportProgressDialog(
                title = "Analyzing workbook",
                message =
                    "Scanning sheets, headers and columns…"
            )
        }

        is StudentImportUiState
        .MappingRequired -> {
            ImportMappingDialog(
                state = importState,
                onSheetSelected = {
                    onIntent(
                        StudentIntent
                            .ImportSheetSelected(it)
                    )
                },
                onNameColumnSelected = {
                    onIntent(
                        StudentIntent
                            .ImportNameColumnSelected(it)
                    )
                },
                onUsnColumnSelected = {
                    onIntent(
                        StudentIntent
                            .ImportUsnColumnSelected(it)
                    )
                },
                onConfirm = {
                    onIntent(
                        StudentIntent
                            .ImportMappingConfirmed
                    )
                },
                onDismiss = {
                    onIntent(
                        StudentIntent
                            .ImportCancelled
                    )
                }
            )
        }

        StudentImportUiState
            .CreatingPreview -> {
            ImportProgressDialog(
                title = "Preparing preview",
                message =
                    "Validating student rows…"
            )
        }

        is StudentImportUiState
        .PreviewReady -> {
            ImportPreviewDialog(
                preview = importState.preview,
                onConfirm = {
                    onIntent(
                        StudentIntent
                            .ImportPreviewConfirmed
                    )
                },
                onDismiss = {
                    onIntent(
                        StudentIntent
                            .ImportPreviewDismissed
                    )
                }
            )
        }

        StudentImportUiState.Importing -> {
            ImportProgressDialog(
                title = "Importing students",
                message =
                    "Saving validated students…"
            )
        }

        is StudentImportUiState.Completed -> {
            ImportResultDialog(
                state = importState,
                onDismiss = {
                    onIntent(
                        StudentIntent
                            .ImportResultDismissed
                    )
                }
            )
        }

        is StudentImportUiState.Error -> {
            ImportErrorDialog(
                message = importState.message,
                onDismiss = {
                    onIntent(
                        StudentIntent
                            .ImportErrorDismissed
                    )
                }
            )
        }
    }
}


@Composable
private fun StudentScreenContent(
    uiState: StudentUiState,
    onIntent: (StudentIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(
            horizontal = 16.dp
        )
    ) {
        Spacer(
            modifier = Modifier.height(12.dp)
        )

        ClassInformationCard(
            semester = uiState.semester,
            branch = uiState.branch,
            subject = uiState.subject,
            studentCount = uiState.students.size
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        when {
            uiState.isLoading -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            uiState.screenError != null -> {
                ErrorContent(
                    message = uiState.screenError,
                    onRetry = {
                        onIntent(
                            StudentIntent.RetryClicked
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            uiState.isEmpty -> {
                EmptyStudentContent(
                    onAddStudent = {
                        onIntent(
                            StudentIntent.AddStudentClicked
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            else -> {
                StudentList(
                    students = uiState.students,
                    onDeleteStudent = { student ->
                        onIntent(
                            StudentIntent.DeleteStudentClicked(
                                student = student
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}
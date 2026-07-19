package com.abdulla.nsspda.attendance.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.details.components.AttendanceDetailsContent
import com.abdulla.nsspda.attendance.presentation.details.components.AttendanceDetailsDialogs
import com.abdulla.nsspda.attendance.presentation.details.components.AttendanceDetailsMenu
import com.abdulla.nsspda.attendance.presentation.details.components.AttendanceDetailsSaveBar
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDetailsScreen(
    uiState: AttendanceDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (AttendanceDetailsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by rememberSaveable {
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
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.isEditing) {
                                "Edit attendance"
                            } else {
                                "Attendance details"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        uiState.date?.let { date ->
                            Text(
                                text = date.format(
                                    DateTimeFormatter.ofLocalizedDate(
                                        FormatStyle.MEDIUM
                                    )
                                ),
                                style =
                                    MaterialTheme.typography.labelMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        enabled = !uiState.isBusy,
                        onClick = {
                            onIntent(
                                AttendanceDetailsIntent.BackClicked
                            )
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            enabled = !uiState.isBusy,
                            onClick = {
                                onIntent(
                                    AttendanceDetailsIntent
                                        .CancelEditingClicked
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription =
                                    "Cancel editing"
                            )
                        }
                    } else {
                        Box {
                            IconButton(
                                enabled =
                                    !uiState.isBusy &&
                                            uiState.attendance.isNotEmpty(),
                                onClick = {
                                    isMenuExpanded = true
                                }
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Default.MoreVert,
                                    contentDescription =
                                        "More options"
                                )
                            }

                            AttendanceDetailsMenu(
                                expanded = isMenuExpanded,
                                isBusy = uiState.isBusy,
                                onDismiss = {
                                    isMenuExpanded = false
                                },
                                onEdit = {
                                    isMenuExpanded = false

                                    onIntent(
                                        AttendanceDetailsIntent
                                            .EditClicked
                                    )
                                },
                                onExport = {
                                    isMenuExpanded = false

                                    onIntent(
                                        AttendanceDetailsIntent
                                            .ExportClicked
                                    )
                                },
                                onDelete = {
                                    isMenuExpanded = false

                                    onIntent(
                                        AttendanceDetailsIntent
                                            .DeleteClicked
                                    )
                                }
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
            )
        },
        bottomBar = {
            if (uiState.isEditing) {
                AttendanceDetailsSaveBar(
                    uiState = uiState,
                    onSave = {
                        onIntent(
                            AttendanceDetailsIntent.SaveClicked
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        AttendanceDetailsContent(
            uiState = uiState,
            contentPadding = innerPadding,
            onIntent = onIntent
        )
    }

    AttendanceDetailsDialogs(
        uiState = uiState,
        onIntent = onIntent
    )

    if (uiState.isExporting) {
        AlertDialog(
            onDismissRequest = {
                // Export cannot be dismissed midway.
            },
            title = {
                Text("Creating attendance report")
            },
            text = {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )

                    Text(
                        "Preparing the Excel workbook…"
                    )
                }
            },
            confirmButton = {}
        )
    }
}
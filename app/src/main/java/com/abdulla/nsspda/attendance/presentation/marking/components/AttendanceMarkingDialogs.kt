package com.abdulla.nsspda.attendance.presentation.marking.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingIntent
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingUiState

@Composable
fun AttendanceMarkingDialogs(
    uiState: AttendanceMarkingUiState,
    onIntent: (AttendanceMarkingIntent) -> Unit
) {
    if (uiState.showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = {
                onIntent(
                    AttendanceMarkingIntent
                        .OverwriteDismissed
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            },
            title = {
                Text("Update existing attendance?")
            },
            text = {
                Text(
                    "Attendance already exists for this date. Your current selections will replace its present and absent values."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onIntent(
                            AttendanceMarkingIntent
                                .OverwriteConfirmed
                        )
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onIntent(
                            AttendanceMarkingIntent
                                .OverwriteDismissed
                        )
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showDiscardChangesDialog) {
        AlertDialog(
            onDismissRequest = {
                onIntent(
                    AttendanceMarkingIntent
                        .DiscardChangesDismissed
                )
            },
            title = {
                Text("Discard attendance changes?")
            },
            text = {
                Text(
                    "Changing the date will discard the attendance changes you have not saved."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onIntent(
                            AttendanceMarkingIntent
                                .DiscardChangesConfirmed
                        )
                    }
                ) {
                    Text("Discard and continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onIntent(
                            AttendanceMarkingIntent
                                .DiscardChangesDismissed
                        )
                    }
                ) {
                    Text("Keep editing")
                }
            }
        )
    }

    if (uiState.showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                onIntent(
                    AttendanceMarkingIntent.ExitDismissed
                )
            },
            title = {
                Text("Leave without saving?")
            },
            text = {
                Text(
                    "Your attendance changes have not been saved."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onIntent(
                            AttendanceMarkingIntent.ExitConfirmed
                        )
                    }
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onIntent(
                            AttendanceMarkingIntent.ExitDismissed
                        )
                    }
                ) {
                    Text("Stay")
                }
            }
        )
    }
}
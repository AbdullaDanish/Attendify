package com.abdulla.nsspda.attendance.presentation.details.components
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsIntent
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsUiState

@Composable
fun AttendanceDetailsDialogs(
    uiState: AttendanceDetailsUiState,
    onIntent: (AttendanceDetailsIntent) -> Unit
) {
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeleting) {
                    onIntent(
                        AttendanceDetailsIntent
                            .DeleteDismissed
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Delete attendance session?")
            },
            text = {
                Text(
                    "This will permanently delete all attendance records for this date."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isDeleting,
                    onClick = {
                        onIntent(
                            AttendanceDetailsIntent
                                .DeleteConfirmed
                        )
                    }
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Delete",
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isDeleting,
                    onClick = {
                        onIntent(
                            AttendanceDetailsIntent
                                .DeleteDismissed
                        )
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = {
                onIntent(
                    AttendanceDetailsIntent
                        .DiscardDismissed
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
            },
            title = {
                Text("Discard changes?")
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
                            AttendanceDetailsIntent
                                .DiscardConfirmed
                        )
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onIntent(
                            AttendanceDetailsIntent
                                .DiscardDismissed
                        )
                    }
                ) {
                    Text("Keep editing")
                }
            }
        )
    }
}
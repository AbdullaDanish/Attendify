package com.abdulla.nsspda.student.presentation.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.student.presentation.StudentUiState

@Composable
fun DeleteAllStudentsDialog(
    uiState: StudentUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!uiState.isSubmitting) {
                onDismiss()
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Delete all students?")
        },
        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text =
                        "This will remove ${uiState.students.size} students from:"
                )

                Text(
                    text = buildString {
                        append(uiState.semester)
                        append(" • ")
                        append(uiState.branch)
                        append(" • ")
                        append(uiState.subject)
                    },
                    fontWeight = FontWeight.SemiBold
                )

                HorizontalDivider()

                Text(
                    text =
                        "This action cannot be undone. Existing attendance records will remain available."
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !uiState.isSubmitting,
                onClick = onConfirm
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Delete all",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !uiState.isSubmitting,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.abdulla.nsspda.semester.data.Semester

@Composable
fun DeleteSemesterDialog(
    semester: Semester,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        title = {
            Text("Delete class?")
        },
        text = {
            Text(
                "Delete ${semester.subject}, " +
                        "${semester.branch}, " +
                        "${semester.semester}? " +
                        "Associated student or attendance data " +
                        "may also be affected."
            )
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onConfirm
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
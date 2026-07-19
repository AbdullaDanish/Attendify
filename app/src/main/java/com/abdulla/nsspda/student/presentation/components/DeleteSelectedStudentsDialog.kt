package com.abdulla.nsspda.student.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteSelectedStudentsDialog(
    selectedCount: Int,
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
        icon = {
            Icon(
                imageVector =
                    Icons.Default.DeleteOutline,
                contentDescription = null,
                tint =
                    MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = if (selectedCount == 1) {
                    "Delete selected student?"
                } else {
                    "Delete $selectedCount students?"
                }
            )
        },
        text = {
            Text(
                text =
                    "The selected student records will be removed from this class. This action cannot be undone."
            )
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onConfirm
            ) {
                Text(
                    text = "Delete",
                    color =
                        MaterialTheme.colorScheme.error
                )
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
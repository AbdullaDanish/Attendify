package com.abdulla.nsspda.attendance.presentation.details.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AttendanceDetailsMenu(
    expanded: Boolean,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = {
                Text("Edit attendance")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            },
            enabled = !isBusy,
            onClick = onEdit
        )

        DropdownMenuItem(
            text = {
                Text("Export attendance")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null
                )
            },
            enabled = !isBusy,
            onClick = onExport
        )

        HorizontalDivider()

        DropdownMenuItem(
            text = {
                Text(
                    text = "Delete attendance",
                    color = MaterialTheme.colorScheme.error
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            enabled = !isBusy,
            onClick = onDelete
        )
    }
}
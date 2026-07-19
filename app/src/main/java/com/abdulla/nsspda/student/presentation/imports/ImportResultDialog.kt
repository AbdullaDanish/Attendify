package com.abdulla.nsspda.student.presentation.imports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.student.presentation.StudentImportUiState

@Composable
fun ImportResultDialog(
    state: StudentImportUiState.Completed,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preview = state.preview
    val result = state.databaseResult

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                if (result.importedCount > 0) {
                    "Import completed"
                } else {
                    "No new students imported"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                ImportResultItem(
                    label = "Valid rows found",
                    value = preview.validRows.size
                )

                ImportResultItem(
                    label = "Students imported",
                    value = result.importedCount
                )

                ImportResultItem(
                    label = "Already in this class",
                    value = result.existingStudentCount
                )

                ImportResultItem(
                    label = "Duplicates inside file",
                    value = preview.duplicateInsideFileCount
                )

                ImportResultItem(
                    label = "Invalid rows skipped",
                    value = preview.invalidDataCount
                )

                HorizontalDivider()

                Text(
                    text = when {
                        result.importedCount > 0 ->
                            "The student list has been updated."

                        result.existingStudentCount > 0 ->
                            "All valid students were already present in this class."

                        else ->
                            "No valid new students were available to import."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun ImportResultItem(
    label: String,
    value: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 11.dp
            ),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
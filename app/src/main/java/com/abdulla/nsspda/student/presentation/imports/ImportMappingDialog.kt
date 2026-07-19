package com.abdulla.nsspda.student.presentation.imports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.student.data.imports.SpreadsheetColumn
import com.abdulla.nsspda.student.presentation.StudentImportUiState

@Composable
fun ImportMappingDialog(
    state: StudentImportUiState.MappingRequired,
    onSheetSelected: (Int) -> Unit,
    onNameColumnSelected: (Int) -> Unit,
    onUsnColumnSelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDetection = state.selectedDetection

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = null
            )
        },
        title = {
            Text("Map Excel columns")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Review the detected sheet and select the columns containing student names and USNs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SimpleSelectionDropdown(
                    label = "Sheet or detected table",
                    options = state.detections.mapIndexed { index, detection ->
                        SelectionOption(
                            id = index,
                            label = detection.sheetName,
                            supportingText =
                                "Header row ${detection.headerRowIndex + 1}"
                        )
                    },
                    selectedId = state.selectedDetectionIndex,
                    onSelected = onSheetSelected
                )

                HorizontalDivider()

                if (selectedDetection == null) {
                    Text(
                        text = "No valid sheet is selected.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Detected columns",
                            style = MaterialTheme.typography.titleSmall
                        )

                        SimpleSelectionDropdown(
                            label = "Student name column",
                            options = selectedDetection.availableColumns
                                .toSelectionOptions(),
                            selectedId =
                                state.selectedNameColumnIndex,
                            onSelected = onNameColumnSelected
                        )

                        SimpleSelectionDropdown(
                            label = "USN or student ID column",
                            options = selectedDetection.availableColumns
                                .toSelectionOptions(),
                            selectedId =
                                state.selectedUsnColumnIndex,
                            onSelected = onUsnColumnSelected
                        )

                        if (
                            state.selectedNameColumnIndex != null &&
                            state.selectedNameColumnIndex ==
                            state.selectedUsnColumnIndex
                        ) {
                            Text(
                                text =
                                    "Student name and USN must use different columns.",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = confidenceLabel(
                                selectedDetection.confidence
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = state.canCreatePreview,
                onClick = onConfirm
            ) {
                Text("Create preview")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun List<SpreadsheetColumn>.toSelectionOptions():
        List<SelectionOption> {
    return map { column ->
        SelectionOption(
            id = column.index,
            label = column.displayName.ifBlank {
                "Column ${column.index + 1}"
            },
            supportingText =
                "Column ${column.index + 1}"
        )
    }
}

private fun confidenceLabel(
    confidence: Float
): String {
    return when {
        confidence >= 0.80f ->
            "Automatic detection confidence: High"

        confidence >= 0.55f ->
            "Automatic detection confidence: Medium"

        else ->
            "Automatic detection confidence: Low — please verify the mapping carefully"
    }
}
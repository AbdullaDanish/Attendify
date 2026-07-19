package com.abdulla.nsspda.student.presentation.imports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.student.data.imports.ParsedStudentRow
import com.abdulla.nsspda.student.data.imports.StudentImportPreview

@Composable
fun ImportPreviewDialog(
    preview: StudentImportPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Preview,
                contentDescription = null
            )
        },
        title = {
            Text("Review students")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = preview.metadata.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                ImportSummaryRow(
                    validCount = preview.validRows.size,
                    duplicateCount =
                        preview.duplicateInsideFileCount,
                    invalidCount =
                        preview.invalidDataCount
                )

                HorizontalDivider()

                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleSmall
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 310.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = preview.previewRows,
                        key = { row ->
                            "${row.sourceRowNumber}-${row.usn}"
                        }
                    ) { row ->
                        PreviewStudentCard(row)
                    }
                }

                if (preview.validRows.size > preview.previewRows.size) {
                    Text(
                        text =
                            "${preview.validRows.size - preview.previewRows.size} additional students will also be imported.",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (preview.invalidRows.isNotEmpty()) {
                    Text(
                        text =
                            "Invalid and duplicate rows will be skipped.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = preview.validRows.isNotEmpty(),
                onClick = onConfirm
            ) {
                Text(
                    "Import ${preview.validRows.size} students"
                )
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

@Composable
private fun ImportSummaryRow(
    validCount: Int,
    duplicateCount: Int,
    invalidCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        ImportCountBadge(
            label = "Valid",
            count = validCount,
            modifier = Modifier.weight(1f)
        )

        ImportCountBadge(
            label = "Duplicates",
            count = duplicateCount,
            modifier = Modifier.weight(1f)
        )

        ImportCountBadge(
            label = "Invalid",
            count = invalidCount,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ImportCountBadge(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color =
            MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 10.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun PreviewStudentCard(
    row: ParsedStudentRow
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 1.dp
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = row.studentName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = row.usn,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Excel row ${row.sourceRowNumber}",
                style = MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
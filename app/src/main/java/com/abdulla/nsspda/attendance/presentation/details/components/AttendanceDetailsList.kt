package com.abdulla.nsspda.attendance.presentation.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsIntent
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsUiState

@Composable
fun AttendanceDetailsList(
    uiState: AttendanceDetailsUiState,
    contentPadding: PaddingValues,
    onIntent: (AttendanceDetailsIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top =
                contentPadding.calculateTopPadding() +
                        16.dp,
            end = 16.dp,
            bottom =
                contentPadding.calculateBottomPadding() +
                        24.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        item(
            key = "session-information"
        ) {
            AttendanceDetailsHeaderCard(
                uiState = uiState
            )
        }

        item(
            key = "session-summary"
        ) {
            AttendanceDetailsSummary(
                presentCount = uiState.presentCount,
                absentCount = uiState.absentCount,
                totalCount = uiState.totalCount
            )
        }

        if (uiState.isEditing) {
            item(
                key = "bulk-actions"
            ) {
                AttendanceDetailsBulkActions(
                    enabled = !uiState.isBusy,
                    onMarkAllPresent = {
                        onIntent(
                            AttendanceDetailsIntent
                                .MarkAllPresent
                        )
                    },
                    onMarkAllAbsent = {
                        onIntent(
                            AttendanceDetailsIntent
                                .MarkAllAbsent
                        )
                    }
                )
            }
        }

        item(
            key = "students-heading"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "Students",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                if (uiState.isEditing) {
                    Text(
                        text = "Tap a student to change status",
                        style =
                            MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }
        }

        items(
            items = uiState.attendance,
            key = { item ->
                item.id
            }
        ) { item ->
            AttendanceDetailsStudentCard(
                item = item,
                isEditing = uiState.isEditing,
                enabled = !uiState.isBusy,
                onToggle = {
                    onIntent(
                        AttendanceDetailsIntent
                            .StudentAttendanceToggled(
                                usn = item.usn
                            )
                    )
                }
            )
        }
    }
}
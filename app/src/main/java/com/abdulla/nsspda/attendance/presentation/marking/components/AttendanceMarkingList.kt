package com.abdulla.nsspda.attendance.presentation.marking.components

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
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingIntent
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingUiState

@Composable
fun AttendanceMarkingList(
    uiState: AttendanceMarkingUiState,
    contentPadding: PaddingValues,
    onSelectDate: () -> Unit,
    onIntent: (AttendanceMarkingIntent) -> Unit
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
            key = "attendance-date"
        ) {
            AttendanceDateCard(
                uiState = uiState,
                onClick = onSelectDate
            )
        }

        item(
            key = "attendance-summary"
        ) {
            AttendanceSummaryCards(
                presentCount = uiState.presentCount,
                absentCount = uiState.absentCount,
                totalCount = uiState.totalCount
            )
        }

        item(
            key = "marking-mode"
        ) {
            AttendanceMarkingModeCard(
                selectedMode = uiState.markingMode,
                enabled = !uiState.isSaving,
                onModeSelected = { mode ->
                    onIntent(
                        AttendanceMarkingIntent
                            .MarkingModeChanged(mode)
                    )
                }
            )
        }

        item(
            key = "bulk-actions"
        ) {
            AttendanceBulkActions(
                enabled =
                    !uiState.isSaving &&
                            uiState.students.isNotEmpty(),
                onMarkAllPresent = {
                    onIntent(
                        AttendanceMarkingIntent.MarkAllPresent
                    )
                },
                onMarkAllAbsent = {
                    onIntent(
                        AttendanceMarkingIntent.MarkAllAbsent
                    )
                }
            )
        }

        item(
            key = "students-heading"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Students",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${uiState.totalCount} total",
                    style = MaterialTheme.typography.labelLarge,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(
            items = uiState.students,
            key = { student ->
                student.studentId
            }
        ) { student ->
            AttendanceStudentCard(
                student = student,
                enabled = !uiState.isSaving,
                onToggle = {
                    onIntent(
                        AttendanceMarkingIntent
                            .StudentAttendanceMarkingToggled(
                                usn = student.usn
                            )
                    )
                }
            )
        }
    }
}
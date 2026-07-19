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
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingIntent
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingUiState
import com.abdulla.nsspda.ui.theme.AppSpacing

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
            start = AppSpacing.ScreenHorizontal,
            top = contentPadding.calculateTopPadding() +
                    AppSpacing.Large,
            end = AppSpacing.ScreenHorizontal,
            bottom = contentPadding.calculateBottomPadding() +
                    AppSpacing.Section
        ),
        verticalArrangement =
            Arrangement.spacedBy(AppSpacing.Medium)
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
            key = "bulk-actions"
        ) {
            AttendanceBulkActions(
                enabled =
                    !uiState.isSaving &&
                            uiState.students.isNotEmpty(),
                allPresentSelected =
                    uiState.areAllStudentsPresent,
                allAbsentSelected =
                    uiState.areAllStudentsAbsent,
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
            StudentsSectionHeader(
                totalCount = uiState.totalCount
            )
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

@Composable
private fun StudentsSectionHeader(
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = AppSpacing.Small
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Students",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = if (totalCount == 1) {
                "1 student"
            } else {
                "$totalCount students"
            },
            style = MaterialTheme.typography.labelLarge,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
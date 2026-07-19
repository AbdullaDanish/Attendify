package com.abdulla.nsspda.attendance.presentation.history.components



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.history.AttendanceHistoryUiState
import java.time.LocalDate


@Composable
fun AttendanceHistoryList(
    uiState: AttendanceHistoryUiState,
    contentPadding: PaddingValues,
    onSearchClick: () -> Unit,
    onSessionClicked: (LocalDate) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(
            key = "class-summary"
        ) {
            AttendanceClassSummaryCard(
                semester = uiState.semester,
                branch = uiState.branch,
                subject = uiState.subject,
                sessionCount = uiState.sessionCount
            )
        }

        item(
            key = "history-heading"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp,
                        bottom = 2.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Recorded sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text =
                            "Select a date to view, edit, export or delete its attendance.",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalIconButton(
                    onClick = onSearchClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription =
                            "Search attendance history"
                    )
                }
            }
        }

        items(
            items = uiState.sessions,
            key = { session ->
                session.date.toEpochDay()
            }
        ) { session ->
            AttendanceSessionCard(
                item = session,
                onClick = {
                    onSessionClicked(session.date)
                }
            )
        }
    }
}
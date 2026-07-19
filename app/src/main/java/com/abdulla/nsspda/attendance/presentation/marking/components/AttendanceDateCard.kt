package com.abdulla.nsspda.attendance.presentation.marking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingUiState
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AttendanceDateCard(
    uiState: AttendanceMarkingUiState,
    onClick: () -> Unit
) {
    val formattedDate =
        uiState.selectedDate.format(
            DateTimeFormatter.ofLocalizedDate(
                FormatStyle.FULL
            )
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(11.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Attendance date",
                        style =
                            MaterialTheme.typography.labelLarge,
                        color =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                    )

                    Text(
                        text = formattedDate,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    enabled = !uiState.isSaving,
                    onClick = onClick
                ) {
                    Text("Change")
                }
            }

            if (uiState.hasExistingAttendance) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color =
                        MaterialTheme.colorScheme
                            .tertiaryContainer
                ) {
                    Text(
                        text =
                            "Attendance already exists for this date. Saving changes will update it.",
                        modifier = Modifier.padding(12.dp),
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onTertiaryContainer
                    )
                }
            }
        }
    }
}
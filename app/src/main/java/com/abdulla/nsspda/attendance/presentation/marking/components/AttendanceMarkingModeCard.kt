package com.abdulla.nsspda.attendance.presentation.marking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingMode

@Composable
fun AttendanceMarkingModeCard(
    selectedMode: AttendanceMarkingMode,
    enabled: Boolean,
    onModeSelected: (AttendanceMarkingMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick marking mode",
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = when (selectedMode) {
                    AttendanceMarkingMode.MARK_ABSENTEES ->
                        "Students start as present. Tap only the absent students."

                    AttendanceMarkingMode.MARK_PRESENTEES ->
                        "Students start as absent. Tap only the present students."
                },
                style = MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected =
                        selectedMode ==
                                AttendanceMarkingMode
                                    .MARK_ABSENTEES,
                    enabled = enabled,
                    onClick = {
                        onModeSelected(
                            AttendanceMarkingMode
                                .MARK_ABSENTEES
                        )
                    },
                    label = {
                        Text("Mark absentees")
                    },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected =
                        selectedMode ==
                                AttendanceMarkingMode
                                    .MARK_PRESENTEES,
                    enabled = enabled,
                    onClick = {
                        onModeSelected(
                            AttendanceMarkingMode
                                .MARK_PRESENTEES
                        )
                    },
                    label = {
                        Text("Mark presentees")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
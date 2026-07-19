package com.abdulla.nsspda.attendance.presentation.percentage.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.percentage.AttendancePercentageUiState
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AttendanceDateRangeCard(
    uiState: AttendancePercentageUiState,
    onStartDateClicked: () -> Unit,
    onEndDateClicked: () -> Unit,
    onCalculate: () -> Unit
) {
    val formatter =
        DateTimeFormatter.ofLocalizedDate(
            FormatStyle.MEDIUM
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color =
                        MaterialTheme.colorScheme.primary
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

                Column {
                    Text(
                        text = "Analytics date range",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text =
                            "Choose the attendance period to analyze.",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                                .copy(alpha = 0.76f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                DateSelectionField(
                    label = "Start date",
                    value =
                        uiState.startDate.format(
                            formatter
                        ),
                    enabled =
                        !uiState.isLoading &&
                                !uiState.isExporting,
                    onClick = onStartDateClicked,
                    modifier = Modifier.weight(1f)
                )

                DateSelectionField(
                    label = "End date",
                    value =
                        uiState.endDate.format(
                            formatter
                        ),
                    enabled =
                        !uiState.isLoading &&
                                !uiState.isExporting,
                    onClick = onEndDateClicked,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!uiState.isDateRangeValid) {
                Text(
                    text =
                        "Start date cannot be after end date.",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }

            Button(
                enabled = uiState.canCalculate,
                onClick = onCalculate,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    if (uiState.hasCalculated) {
                        "Recalculate analytics"
                    } else {
                        "Calculate analytics"
                    }
                )
            }
        }
    }
}

@Composable
private fun DateSelectionField(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color =
            MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style =
                    MaterialTheme.typography.labelMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Text(
                text = value,
                style =
                    MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
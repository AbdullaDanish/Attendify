package com.abdulla.nsspda.attendance.presentation.percentage.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.domain.model.AttendanceAnalytics
import kotlin.math.roundToInt

@Composable
fun AttendanceOverviewSection(
    analytics: AttendanceAnalytics
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(20.dp)
            ) {
                AttendanceAverageIndicator(
                    percentage =
                        analytics.classAveragePercentage
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Class average",
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text =
                            "${analytics.totalPresentEntries} present entries from " +
                                    "${analytics.totalPresentEntries + analytics.totalAbsentEntries} attendance entries.",
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    analytics.highestPercentage?.let {
                        Text(
                            text =
                                "Highest student attendance: ${it.formatPercentage()}",
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }

                    analytics.lowestPercentage?.let {
                        Text(
                            text =
                                "Lowest student attendance: ${it.formatPercentage()}",
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            AnalyticsMetricCard(
                label = "Sessions",
                value = analytics.sessionCount.toString(),
                modifier = Modifier.weight(1f)
            )

            AnalyticsMetricCard(
                label = "Students",
                value = analytics.studentCount.toString(),
                modifier = Modifier.weight(1f)
            )

            AnalyticsMetricCard(
                label = "Need attention",
                value =
                    analytics.studentsNeedingAttention
                        .size
                        .toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AttendanceAverageIndicator(
    percentage: Double
) {
    val safePercentage =
        percentage.coerceIn(0.0, 100.0)

    val progress =
        (safePercentage / 100.0).toFloat()

    val progressColor =
        riskContainerColor(
            riskLevelForPercentage(
                percentage = safePercentage
            )
        )

    Box(
        modifier = Modifier
            .size(116.dp)
            .semantics {
                contentDescription =
                    "Class average attendance is ${safePercentage.formatPercentage()}"
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 12.dp.toPx()

            drawArc(
                color =
                    progressColor.copy(
                        alpha = 0.18f
                    ),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle =
                    360f * progress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    "${safePercentage.roundToInt()}%",
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Average",
                style =
                    MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnalyticsMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 14.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                style =
                    MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}
package com.abdulla.nsspda.attendance.presentation.percentage.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.domain.model.AttendanceTrendPoint
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AttendanceTrendCard(
    trend: List<AttendanceTrendPoint>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Attendance trend",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Class attendance percentage for each recorded session.",
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            AttendanceTrendChart(
                trend = trend
            )

            val latest = trend.lastOrNull()

            if (latest != null) {
                Text(
                    text =
                        "Latest session: ${latest.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))} — " +
                                latest.percentage.formatPercentage(),
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AttendanceTrendChart(
    trend: List<AttendanceTrendPoint>
) {
    val lineColor =
        MaterialTheme.colorScheme.primary

    val gridColor =
        MaterialTheme.colorScheme.outlineVariant

    val pointColor =
        MaterialTheme.colorScheme.primary

    val description = trend.joinToString(
        separator = ". "
    ) { point ->
        "${point.date}: ${point.percentage.formatPercentage()}"
    }
    val surfaceColor =
        MaterialTheme.colorScheme.surface

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .semantics {
                contentDescription =
                    "Attendance trend. $description"
            }
    ) {
        if (trend.isEmpty()) {
            return@Canvas
        }

        val leftPadding = 18.dp.toPx()
        val rightPadding = 12.dp.toPx()
        val topPadding = 16.dp.toPx()
        val bottomPadding = 18.dp.toPx()

        val chartWidth =
            size.width -
                    leftPadding -
                    rightPadding

        val chartHeight =
            size.height -
                    topPadding -
                    bottomPadding

        listOf(
            0f,
            0.25f,
            0.5f,
            0.75f,
            1f
        ).forEach { fraction ->
            val y =
                topPadding +
                        chartHeight *
                        (1f - fraction)

            drawLine(
                color = gridColor,
                start = Offset(
                    x = leftPadding,
                    y = y
                ),
                end = Offset(
                    x = size.width -
                            rightPadding,
                    y = y
                ),
                strokeWidth = 1.dp.toPx()
            )
        }

        val points =
            trend.mapIndexed { index, item ->
                val x =
                    if (trend.size == 1) {
                        leftPadding +
                                chartWidth / 2f
                    } else {
                        leftPadding +
                                chartWidth *
                                index.toFloat() /
                                (trend.size - 1).toFloat()
                    }

                val normalized =
                    (
                            item.percentage
                                .coerceIn(0.0, 100.0) /
                                    100.0
                            ).toFloat()

                val y =
                    topPadding +
                            chartHeight *
                            (1f - normalized)

                Offset(
                    x = x,
                    y = y
                )
            }

        points.zipWithNext().forEach {
                (start, end) ->

            drawLine(
                color = lineColor,
                start = start,
                end = end,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        points.forEach { point ->
            drawCircle(
                color = pointColor,
                radius = 5.dp.toPx(),
                center = point
            )

            drawCircle(
                color = surfaceColor,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}
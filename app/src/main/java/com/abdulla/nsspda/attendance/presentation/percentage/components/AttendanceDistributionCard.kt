package com.abdulla.nsspda.attendance.presentation.percentage.components


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.domain.model.AttendanceDistribution
import com.abdulla.nsspda.attendance.domain.model.AttendanceRiskLevel


@Composable
fun AttendanceDistributionCard(
    distribution: AttendanceDistribution
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
                text = "Student risk distribution",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Students grouped by their attendance percentage.",
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            AttendanceDistributionChart(
                distribution = distribution
            )

            DistributionLegendItem(
                label = "Excellent · 90% and above",
                count = distribution.excellentCount,
                riskLevel =
                    AttendanceRiskLevel.EXCELLENT
            )

            DistributionLegendItem(
                label = "Good · 75% to 89%",
                count = distribution.goodCount,
                riskLevel =
                    AttendanceRiskLevel.GOOD
            )

            DistributionLegendItem(
                label = "Warning · 60% to 74%",
                count = distribution.warningCount,
                riskLevel =
                    AttendanceRiskLevel.WARNING
            )

            DistributionLegendItem(
                label = "Critical · Below 60%",
                count = distribution.criticalCount,
                riskLevel =
                    AttendanceRiskLevel.CRITICAL
            )
        }
    }
}

@Composable
private fun AttendanceDistributionChart(
    distribution: AttendanceDistribution
) {
    val total =
        distribution.totalStudents

    val excellentColor =
        riskContainerColor(
            AttendanceRiskLevel.EXCELLENT
        )

    val goodColor =
        riskContainerColor(
            AttendanceRiskLevel.GOOD
        )

    val warningColor =
        riskContainerColor(
            AttendanceRiskLevel.WARNING
        )

    val criticalColor =
        riskContainerColor(
            AttendanceRiskLevel.CRITICAL
        )

    val description =
        "Risk distribution. " +
                "${distribution.excellentCount} excellent, " +
                "${distribution.goodCount} good, " +
                "${distribution.warningCount} warning, " +
                "${distribution.criticalCount} critical."

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .semantics {
                contentDescription = description
            }
    ) {
        if (total <= 0) {
            return@Canvas
        }

        val cornerRadius =
            12.dp.toPx()

        var startX = 0f

        val values = listOf(
            distribution.excellentCount to
                    excellentColor,
            distribution.goodCount to
                    goodColor,
            distribution.warningCount to
                    warningColor,
            distribution.criticalCount to
                    criticalColor
        )

        values.forEach { (count, color) ->
            if (count <= 0) {
                return@forEach
            }

            val width =
                size.width *
                        count.toFloat() /
                        total.toFloat()

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = startX,
                    y = 0f
                ),
                size = Size(
                    width = width,
                    height = size.height
                ),
                cornerRadius =
                    CornerRadius(
                        x = cornerRadius,
                        y = cornerRadius
                    )
            )

            startX += width
        }
    }
}

@Composable
private fun DistributionLegendItem(
    label: String,
    count: Int,
    riskLevel: AttendanceRiskLevel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color =
                        riskContainerColor(
                            riskLevel
                        ),
                    shape = CircleShape
                )
        )

        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = count.toString(),
            style =
                MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
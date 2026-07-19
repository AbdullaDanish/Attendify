package com.abdulla.nsspda.attendance.presentation.percentage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.domain.model.StudentAttendanceAnalytics

@Composable
 fun StudentAttendanceProgressCard(
    student: StudentAttendanceAnalytics
) {
    val progress =
        (
                student.percentage
                    .coerceIn(0.0, 100.0) /
                        100.0
                ).toFloat()

    val riskColor =
        riskContainerColor(
            student.riskLevel
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "${student.studentName}, ${student.usn}, " +
                            "${student.percentage.formatPercentage()}, " +
                            "${student.presentSessions} present and " +
                            "${student.absentSessions} absent sessions, " +
                            "${student.riskLevel.displayName()} status."
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student.studentName,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = student.usn,
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color =
                        riskColor.copy(
                            alpha = 0.18f
                        )
                ) {
                    Text(
                        text =
                            student.riskLevel.displayName(),
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        style =
                            MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
            }

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        student.percentage.formatPercentage(),
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text =
                        "${student.presentSessions} / ${student.totalSessions} present",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                color = riskColor,
                trackColor = riskColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
            )

            Text(
                text =
                    "${student.absentSessions} absent ${
                        if (student.absentSessions == 1) {
                            "session"
                        } else {
                            "sessions"
                        }
                    }",
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}
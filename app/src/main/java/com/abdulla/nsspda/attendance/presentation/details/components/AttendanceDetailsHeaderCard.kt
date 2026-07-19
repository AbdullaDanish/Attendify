package com.abdulla.nsspda.attendance.presentation.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsUiState
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AttendanceDetailsHeaderCard(
    uiState: AttendanceDetailsUiState
) {
    val formattedDate = uiState.date?.format(
        DateTimeFormatter.ofLocalizedDate(
            FormatStyle.FULL
        )
    ).orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = uiState.subject.ifBlank {
                            "Attendance session"
                        },
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = listOf(
                            uiState.semester,
                            uiState.branch
                        ).filter {
                            it.isNotBlank()
                        }.joinToString(" • "),
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                                .copy(alpha = 0.8f)
                    )
                }
            }

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer
                        .copy(alpha = 0.15f)
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (uiState.isEditing) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color =
                        MaterialTheme.colorScheme
                            .tertiaryContainer
                ) {
                    Text(
                        text =
                            "Editing mode is active. Save your changes before leaving.",
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
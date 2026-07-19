package com.abdulla.nsspda.attendance.presentation.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.attendance.presentation.details.AttendanceDetailsItem

@Composable
fun AttendanceDetailsStudentCard(
    item: AttendanceDetailsItem,
    isEditing: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val statusText = if (item.isPresent) {
        "Present"
    } else {
        "Absent"
    }

    val containerColor = if (item.isPresent) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Checkbox
                contentDescription =
                    "${item.studentName}, ${item.usn}, $statusText"
            }
            .clickable(
                enabled = enabled && isEditing,
                onClick = onToggle
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEditing) {
                2.dp
            } else {
                0.dp
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (item.isPresent) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.error
                }
            ) {
                Icon(
                    imageVector = if (item.isPresent) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = if (item.isPresent) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onError
                    },
                    modifier = Modifier.padding(11.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.studentName,
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.usn,
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color = if (item.isPresent) {
                        MaterialTheme.colorScheme
                            .onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme
                            .onErrorContainer
                    }
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = statusText,
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isPresent) {
                        MaterialTheme.colorScheme
                            .onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme
                            .onErrorContainer
                    }
                )

                if (isEditing) {
                    Text(
                        text = "Tap to change",
                        style =
                            MaterialTheme.typography.labelSmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }
        }
    }
}
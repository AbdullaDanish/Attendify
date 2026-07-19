package com.abdulla.nsspda.attendance.presentation.marking.components


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
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceStudentItem

@Composable
fun AttendanceStudentCard(
    student: AttendanceStudentItem,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val statusText =
        if (student.isPresent) {
            "Present"
        } else {
            "Absent"
        }

    Card(
        onClick = onToggle,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Checkbox
                contentDescription =
                    "${student.studentName}, ${student.usn}, $statusText"
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (student.isPresent) {
                    MaterialTheme.colorScheme
                        .secondaryContainer
                } else {
                    MaterialTheme.colorScheme
                        .errorContainer
                }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color =
                    if (student.isPresent) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
            ) {
                Icon(
                    imageVector =
                        if (student.isPresent) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Close
                        },
                    contentDescription = null,
                    tint =
                        if (student.isPresent) {
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
                        if (student.isPresent) {
                            MaterialTheme.colorScheme
                                .onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme
                                .onErrorContainer
                        }
                )
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color =
                    if (student.isPresent) {
                        MaterialTheme.colorScheme
                            .onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme
                            .onErrorContainer
                    }
            )
        }
    }
}
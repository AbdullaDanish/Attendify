package com.abdulla.nsspda.attendance.presentation.marking.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.ui.theme.AppAnimation
import com.abdulla.nsspda.ui.theme.AppSpacing

@Composable
fun AttendanceBulkActions(
    enabled: Boolean,
    allPresentSelected: Boolean,
    allAbsentSelected: Boolean,
    onMarkAllPresent: () -> Unit,
    onMarkAllAbsent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neutralContainer =
        MaterialTheme.colorScheme.surface

    val neutralContent =
        MaterialTheme.colorScheme.onSurfaceVariant

    val neutralBorder =
        MaterialTheme.colorScheme.outlineVariant

    val presentContainer by animateColorAsState(
        targetValue = if (allPresentSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            neutralContainer
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "present_container"
    )

    val presentContent by animateColorAsState(
        targetValue = if (allPresentSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            neutralContent
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "present_content"
    )

    val presentBorder by animateColorAsState(
        targetValue = if (allPresentSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            neutralBorder
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "present_border"
    )

    val absentContainer by animateColorAsState(
        targetValue = if (allAbsentSelected) {
            MaterialTheme.colorScheme.error
        } else {
            neutralContainer
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "absent_container"
    )

    val absentContent by animateColorAsState(
        targetValue = if (allAbsentSelected) {
            MaterialTheme.colorScheme.onError
        } else {
            neutralContent
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "absent_content"
    )

    val absentBorder by animateColorAsState(
        targetValue = if (allAbsentSelected) {
            MaterialTheme.colorScheme.error
        } else {
            neutralBorder
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "absent_border"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(AppSpacing.Medium)
    ) {
        Button(
            enabled = enabled,
            onClick = onMarkAllPresent,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 50.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = presentContainer,
                contentColor = presentContent,
                disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor =
                    MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = BorderStroke(
                width = 1.dp,
                color = presentBorder
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null
            )

            Text(
                text = "All present",
                modifier = Modifier.padding(
                    start = AppSpacing.Small
                ),
                style =
                    MaterialTheme.typography.labelLarge
            )
        }

        Button(
            enabled = enabled,
            onClick = onMarkAllAbsent,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 50.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = absentContainer,
                contentColor = absentContent,
                disabledContainerColor =
                    MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor =
                    MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = BorderStroke(
                width = 1.dp,
                color = absentBorder
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )

            Text(
                text = "All absent",
                modifier = Modifier.padding(
                    start = AppSpacing.Small
                ),
                style =
                    MaterialTheme.typography.labelLarge
            )
        }
    }
}
package com.abdulla.nsspda.student.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.student.data.local.Student
import com.abdulla.nsspda.ui.theme.AppAnimation

@Composable
fun StudentCard(
    student: Student,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "student_card_border"
    )

    val borderWidth = if (isSelected) {
        1.5.dp
    } else {
        0.dp
    }

    val borderWidthPx = with(LocalDensity.current) {
        borderWidth.toPx()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (isSelected) {
                    drawRoundRect(
                        color = borderColor,
                        size = Size(
                            width = size.width,
                            height = size.height
                        ),
                        cornerRadius =
                            androidx.compose.ui.geometry.CornerRadius(
                                x = 18.dp.toPx(),
                                y = 18.dp.toPx()
                            ),
                        style = Stroke(
                            width = borderWidthPx
                        )
                    )
                }
            }
            .clip(shape)
            .clickable(
                enabled = isSelectionMode,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                if (isSelectionMode) {
                    role = Role.Checkbox
                    selected = isSelected
                    contentDescription =
                        "${student.studentName}, " +
                                if (isSelected) {
                                    "selected"
                                } else {
                                    "not selected"
                                }
                }
            },
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) {
                1.dp
            } else {
                2.dp
            }
        )
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            headlineContent = {
                Text(
                    text = student.studentName.ifBlank {
                        "Unnamed student"
                    },
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = student.usn.ifBlank {
                        "USN not available"
                    },
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                if (isSelectionMode) {
                    SelectionIndicator(
                        selected = isSelected
                    )
                } else {
                    StudentInitialAvatar(
                        studentName = student.studentName
                    )
                }
            },
            trailingContent = {
                if (!isSelectionMode) {
                    FilledTonalIconButton(
                        onClick = onDelete,
                        modifier = Modifier.semantics {
                            contentDescription =
                                "Delete ${student.studentName}"
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint =
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "selection_indicator_container"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(
            durationMillis = AppAnimation.Fast
        ),
        label = "selection_indicator_border"
    )

    Surface(
        modifier = modifier.size(42.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint =
                        MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
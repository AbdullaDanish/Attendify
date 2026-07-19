package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.ui.theme.AppAnimation
import com.abdulla.nsspda.ui.theme.AppSpacing
import kotlinx.coroutines.delay

@Composable
fun LiveAcademicMessage(
    modifier: Modifier = Modifier
) {
    val messages = remember {
        listOf(
            "Classes stay organized here",
            "Attendance records remain accessible",
            "Student progress stays easy to review"
        )
    }

    var index by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3_500)
            index = (index + 1) % messages.size
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppSpacing.Large,
                vertical = AppSpacing.Medium
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(AppSpacing.Medium)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            AnimatedContent(
                targetState = messages[index],
                transitionSpec = {
                    (
                            slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(
                                    durationMillis = AppAnimation.Normal,
                                    easing = FastOutSlowInEasing
                                )
                            ) + fadeIn(
                                animationSpec = tween(
                                    AppAnimation.Normal
                                )
                            )
                            ) togetherWith (
                            slideOutVertically(
                                targetOffsetY = { -it / 3 },
                                animationSpec = tween(
                                    AppAnimation.Fast
                                )
                            ) + fadeOut(
                                animationSpec = tween(
                                    AppAnimation.Fast
                                )
                            )
                            )
                },
                label = "academic_message"
            ) { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
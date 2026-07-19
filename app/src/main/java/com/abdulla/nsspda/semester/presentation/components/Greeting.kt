package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.abdulla.nsspda.ui.theme.AppSpacing
import kotlinx.coroutines.delay
import java.time.LocalTime

data class GreetingUi(
    val text: String
)

private fun currentGreeting(): GreetingUi {
    return when (LocalTime.now().hour) {
        in 5..11 -> GreetingUi(
            text = "Good morning"
        )

        in 12..16 -> GreetingUi(
            text = "Good afternoon"
        )

        else -> GreetingUi(
            text = "Good evening"
        )
    }
}

@Composable
fun rememberGreeting(): GreetingUi {
    var greeting by remember {
        mutableStateOf(currentGreeting())
    }

    LaunchedEffect(Unit) {
        while (true) {
            greeting = currentGreeting()

            // Refreshing every minute ensures that the greeting updates
            // when the app remains open across a time boundary.
            delay(60_000)
        }
    }

    return greeting
}

@Composable
fun GreetingWithWavingHand(
    greeting: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    fontWeight: FontWeight? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(AppSpacing.XSmall)
    ) {
        Text(
            text = greeting,
            style = style,
            fontWeight = fontWeight
        )

        WavingHandEmoji(
            style = style
        )
    }
}

@Composable
private fun WavingHandEmoji(
    modifier: Modifier = Modifier,
    style: TextStyle
) {
    val rotation = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {
        while (true) {
            // Wave once when first displayed and then every 10 seconds.
            val waveAngles = listOf(
                18f,
                -14f,
                16f,
                -8f,
                0f
            )

            waveAngles.forEachIndexed { index, angle ->
                rotation.animateTo(
                    targetValue = angle,
                    animationSpec = tween(
                        durationMillis = if (
                            index == waveAngles.lastIndex
                        ) {
                            170
                        } else {
                            130
                        },
                        easing = FastOutSlowInEasing
                    )
                )
            }

            delay(3_000)
        }
    }

    Text(
        text = "👋",
        style = style,
        modifier = modifier.graphicsLayer {
            rotationZ = rotation.value

            // Makes the rotation appear to originate from the wrist.
            transformOrigin = TransformOrigin(
                pivotFractionX = 0.2f,
                pivotFractionY = 0.9f
            )
        }
    )
}
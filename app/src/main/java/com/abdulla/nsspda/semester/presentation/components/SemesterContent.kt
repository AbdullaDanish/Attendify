package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.semester.data.Semester
import com.abdulla.nsspda.semester.presentation.SemesterIntent
import com.abdulla.nsspda.semester.presentation.SemesterUiState
import com.abdulla.nsspda.ui.theme.AppAnimation
import com.abdulla.nsspda.ui.theme.AppSpacing

private enum class SemesterScreenState {
    Loading,
    Empty,
    Classes
}

@Composable
fun SemesterContent(
    uiState: SemesterUiState,
    onIntent: (SemesterIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenState = when {
        uiState.isLoading -> SemesterScreenState.Loading
        uiState.isEmpty -> SemesterScreenState.Empty
        else -> SemesterScreenState.Classes
    }

    AnimatedContent(
        targetState = screenState,
        modifier = modifier,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = AppAnimation.Normal
                )
            ) togetherWith fadeOut(
                animationSpec = tween(
                    durationMillis = AppAnimation.Fast
                )
            )
        },
        label = "semester_screen_state"
    ) { state ->
        when (state) {
            SemesterScreenState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            SemesterScreenState.Empty -> {
                EmptySemesterContent(
                    onAddClassClick = {
                        onIntent(
                            SemesterIntent.AddClassClicked
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            SemesterScreenState.Classes -> {
                ClassesContent(
                    semesters = uiState.semesters,
                    onIntent = onIntent,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ClassesContent(
    semesters: List<Semester>,
    onIntent: (SemesterIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.animateContentSize(
            animationSpec = tween(
                durationMillis = AppAnimation.Normal,
                easing = FastOutSlowInEasing
            )
        ),
        contentPadding = PaddingValues(
            start = AppSpacing.ScreenHorizontal,
            top = AppSpacing.Large,
            end = AppSpacing.ScreenHorizontal,
            bottom = 104.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(AppSpacing.Medium)
    ) {
        item(
            key = "class_section_header"
        ) {
            ClassesSectionHeader(
                classCount = semesters.size
            )
        }

        items(
            items = semesters,
            key = Semester::id
        ) { semester ->
            SemesterCard(
                semester = semester,
                onClick = {
                    onIntent(
                        SemesterIntent.SemesterClicked(
                            semester
                        )
                    )
                },
                onDelete = {
                    onIntent(
                        SemesterIntent.DeleteClicked(
                            semester
                        )
                    )
                },
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(
                        durationMillis = AppAnimation.Normal
                    ),
                    placementSpec = tween(
                        durationMillis = AppAnimation.Normal,
                        easing = FastOutSlowInEasing
                    ),
                    fadeOutSpec = tween(
                        durationMillis = AppAnimation.Fast
                    )
                )
            )
        }
    }
}

@Composable
private fun ClassesSectionHeader(
    classCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                bottom = AppSpacing.XSmall
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Your classes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = if (classCount == 1) {
                    "1 class"
                } else {
                    "$classCount classes"
                },
                modifier = Modifier.padding(
                    horizontal = AppSpacing.Medium,
                    vertical = AppSpacing.Small
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color =
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
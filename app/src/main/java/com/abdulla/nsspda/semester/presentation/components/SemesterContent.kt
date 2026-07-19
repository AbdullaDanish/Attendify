package com.abdulla.nsspda.semester.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.abdulla.nsspda.R
import com.abdulla.nsspda.semester.data.Semester
import com.abdulla.nsspda.semester.presentation.SemesterIntent
import com.abdulla.nsspda.semester.presentation.SemesterUiState

@Composable
fun SemesterContent(
    uiState: SemesterUiState,
    onIntent: (SemesterIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(
                id = R.drawable.pdalogo
            ),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.08f),
            contentScale = ContentScale.Fit
        )

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            uiState.isEmpty -> {
                EmptySemesterContent(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.semesters,
                        key = Semester::id
                    ) { semester ->
                        SemesterCard(
                            semester = semester,
                            onClick = {
                                onIntent(
                                    SemesterIntent
                                        .SemesterClicked(
                                            semester
                                        )
                                )
                            },
                            onDelete = {
                                onIntent(
                                    SemesterIntent
                                        .DeleteClicked(
                                            semester
                                        )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
package com.abdulla.nsspda.classoverview.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ClassOverviewRoute(
    onNavigateBack: () -> Unit,
    onNavigateToStudents: (
        semester: String,
        branch: String,
        subject: String
    ) -> Unit,
    onNavigateToAttendanceHistory: (
        semester: String,
        branch: String,
        subject: String
    ) -> Unit,
    viewModel: ClassOverviewViewModel = hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ClassOverviewEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is ClassOverviewEffect.NavigateToStudents -> {
                    onNavigateToStudents(
                        effect.semester,
                        effect.branch,
                        effect.subject
                    )
                }

                is ClassOverviewEffect
                .NavigateToAttendanceHistory -> {
                    onNavigateToAttendanceHistory(
                        effect.semester,
                        effect.branch,
                        effect.subject
                    )
                }

                is ClassOverviewEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }
            }
        }
    }

    ClassOverviewScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}
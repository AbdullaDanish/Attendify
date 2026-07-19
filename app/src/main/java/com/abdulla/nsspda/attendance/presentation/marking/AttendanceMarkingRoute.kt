package com.abdulla.nsspda.attendance.presentation.marking

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.abdulla.nsspda.attendance.presentation.AttendanceMarkingViewModel

@Composable
fun AttendanceMarkingRoute(
    navController: NavController,
    viewModel: AttendanceMarkingViewModel = hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    BackHandler(
        enabled = !uiState.isSaving
    ) {
        viewModel.onIntent(
            AttendanceMarkingIntent.BackClicked
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AttendanceMarkingEffect.NavigateBack -> {
                    navController.navigateUp()
                }

                is AttendanceMarkingEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }
            }
        }
    }

    AttendanceMarkingScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}
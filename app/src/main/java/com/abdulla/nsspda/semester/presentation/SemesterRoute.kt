package com.abdulla.nsspda.semester.presentation

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdulla.nsspda.navigation.AppRoutes

@Composable
fun SemesterRoute(
    navController: NavController,
    viewModel: SemesterViewModel = hiltViewModel()
) {
    val uiState =
        viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState =
        remember { SnackbarHostState() }

    LaunchedEffect(navController, viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SemesterEffect.NavigateToAttendance -> {
                    // Safety check to prevent duplicate navigation crashes
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        val semester =
                            Uri.encode(effect.semester)

                        val branch =
                            Uri.encode(effect.branch)

                        val subject =
                            Uri.encode(effect.subject)

                        // Using "detailsScreen" to match the route in AppNavHost
                        navController.navigate(
                            AppRoutes.classOverview(
                                semester = semester,
                                branch = branch,
                                subject = subject
                            )
                        )
                    }
                }

                is SemesterEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message
                    )
                }
            }
        }
    }

    SemesterListScreen(
        uiState = uiState.value,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = {
            navController.navigateUp()
        }
    )
}
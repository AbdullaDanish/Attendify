package com.abdulla.nsspda.attendance.presentation.history.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.abdulla.nsspda.navigation.AppRoutes
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceHistorySearchRoute(
    navController: NavController,
    viewModel: AttendanceHistorySearchViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AttendanceHistorySearchEffect
                .NavigateToDetails -> {
                    navController.navigate(
                        AppRoutes.attendanceDetails(
                            semester = effect.semester,
                            branch = effect.branch,
                            subject = effect.subject,
                            date = effect.date.format(
                                DateTimeFormatter
                                    .ISO_LOCAL_DATE
                            )
                        )
                    )
                }
            }
        }
    }

    AttendanceHistorySearchScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = {
            navController.navigateUp()
        }
    )
}
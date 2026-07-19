package com.abdulla.nsspda.attendance.presentation.history

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.abdulla.nsspda.navigation.AppRoutes
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceHistoryRoute(
    navController: NavController,
    viewModel: AttendanceHistoryViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AttendanceHistoryEffect.NavigateBack -> {
                    navController.navigateUp()
                }

                is AttendanceHistoryEffect
                .NavigateToNewAttendance -> {
                    navController.navigate(
                        AppRoutes.attendanceMarking(
                            semester = effect.semester,
                            branch = effect.branch,
                            subject = effect.subject
                        )
                    )
                }

                is AttendanceHistoryEffect
                .NavigateToAttendanceDetails -> {
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

                is AttendanceHistoryEffect
                .NavigateToPercentage -> {
                    navController.navigate(
                        AppRoutes.attendancePercentage(
                            semester = effect.semester,
                            branch = effect.branch,
                            subject = effect.subject
                        )
                    )
                }

                is AttendanceHistoryEffect
                .NavigateToSearch -> {
                    navController.navigate(
                        AppRoutes.attendanceHistorySearch(
                            semester = effect.semester,
                            branch = effect.branch,
                            subject = effect.subject
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    AttendanceHistoryScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}
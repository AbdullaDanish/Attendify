package com.abdulla.nsspda.attendance.presentation.history

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceHistoryRoute(
    navController: NavController,
    viewModel: AttendanceHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AttendanceHistoryEffect.NavigateBack -> {
                    navController.navigateUp()
                }

                is AttendanceHistoryEffect.NavigateToNewAttendance -> {
                    navController.navigate(
                        buildNewAttendanceRoute(
                            semester = effect.semester,
                            branch = effect.branch,
                            subject = effect.subject
                        )
                    )
                }

                is AttendanceHistoryEffect.NavigateToAttendanceDetails -> {
                    navController.navigate(
                        buildAttendanceDetailsRoute(
                            semester = effect.semester,
                            branch = effect.branch,
                            subject = effect.subject,
                            date = effect.date.format(
                                DateTimeFormatter.ISO_LOCAL_DATE
                            )
                        )
                    )
                }

                is AttendanceHistoryEffect.NavigateToPercentage -> {
                    navController.navigate(
                        buildPercentageRoute(
                            semester = effect.semester,
                            branch = effect.branch,
                            subject = effect.subject
                        )
                    )
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

private fun buildNewAttendanceRoute(
    semester: String,
    branch: String,
    subject: String
): String {
    return "attendance_marking/" +
            "${Uri.encode(semester)}/" +
            "${Uri.encode(branch)}/" +
            Uri.encode(subject)
}

private fun buildAttendanceDetailsRoute(
    semester: String,
    branch: String,
    subject: String,
    date: String
): String {
    return "attendance_details/" +
            "${Uri.encode(semester)}/" +
            "${Uri.encode(branch)}/" +
            "${Uri.encode(subject)}/" +
            Uri.encode(date)
}

private fun buildPercentageRoute(
    semester: String,
    branch: String,
    subject: String
): String {
    return "attendance_percentage/" +
            "${Uri.encode(semester)}/" +
            "${Uri.encode(branch)}/" +
            Uri.encode(subject)
}
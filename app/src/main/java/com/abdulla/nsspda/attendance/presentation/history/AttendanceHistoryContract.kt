package com.abdulla.nsspda.attendance.presentation.history

import java.time.LocalDate

data class AttendanceHistoryItem(
    val date: LocalDate,
    val totalCount: Int,
    val presentCount: Int,
    val absentCount: Int
) {
    val attendancePercentage: Double
        get() {
            if (totalCount <= 0) {
                return 0.0
            }

            return presentCount.toDouble() /
                    totalCount.toDouble() *
                    100.0
        }
}

data class AttendanceHistoryUiState(
    val semester: String = "",
    val branch: String = "",
    val subject: String = "",
    val sessions: List<AttendanceHistoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isClassValid: Boolean
        get() {
            return semester.isNotBlank() &&
                    branch.isNotBlank() &&
                    subject.isNotBlank()
        }

    val isEmpty: Boolean
        get() {
            return !isLoading &&
                    errorMessage == null &&
                    sessions.isEmpty()
        }

    val sessionCount: Int
        get() = sessions.size
}

sealed interface AttendanceHistoryIntent {

    data object BackClicked :
        AttendanceHistoryIntent

    data object NewAttendanceClicked :
        AttendanceHistoryIntent

    data object PercentageClicked :
        AttendanceHistoryIntent

    data class AttendanceSessionClicked(
        val date: LocalDate
    ) : AttendanceHistoryIntent

    data object RetryClicked :
        AttendanceHistoryIntent
}

sealed interface AttendanceHistoryEffect {

    data object NavigateBack :
        AttendanceHistoryEffect

    data class NavigateToNewAttendance(
        val semester: String,
        val branch: String,
        val subject: String
    ) : AttendanceHistoryEffect

    data class NavigateToAttendanceDetails(
        val semester: String,
        val branch: String,
        val subject: String,
        val date: LocalDate
    ) : AttendanceHistoryEffect

    data class NavigateToPercentage(
        val semester: String,
        val branch: String,
        val subject: String
    ) : AttendanceHistoryEffect
}
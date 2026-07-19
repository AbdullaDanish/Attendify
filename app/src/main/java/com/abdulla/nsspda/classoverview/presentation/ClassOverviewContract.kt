package com.abdulla.nsspda.classoverview.presentation

data class ClassOverviewUiState(
    val semester: String = "",
    val branch: String = "",
    val subject: String = "",
    val studentCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isClassValid: Boolean
        get() {
            return semester.isNotBlank() &&
                    branch.isNotBlank() &&
                    subject.isNotBlank()
        }

    val hasStudents: Boolean
        get() = studentCount > 0

    val studentCountLabel: String
        get() {
            return when (studentCount) {
                0 -> "No students"
                1 -> "1 student"
                else -> "$studentCount students"
            }
        }

    val studentActionLabel: String
        get() {
            return if (hasStudents) {
                "Manage students"
            } else {
                "Add or import students"
            }
        }

    val canOpenAttendance: Boolean
        get() {
            return hasStudents &&
                    !isLoading &&
                    errorMessage == null
        }
}

sealed interface ClassOverviewIntent {

    data object BackClicked : ClassOverviewIntent

    data object ManageStudentsClicked : ClassOverviewIntent

    data object AttendanceHistoryClicked : ClassOverviewIntent

    data object RetryClicked : ClassOverviewIntent
}

sealed interface ClassOverviewEffect {

    data object NavigateBack : ClassOverviewEffect

    data class NavigateToStudents(
        val semester: String,
        val branch: String,
        val subject: String
    ) : ClassOverviewEffect

    data class NavigateToAttendanceHistory(
        val semester: String,
        val branch: String,
        val subject: String
    ) : ClassOverviewEffect

    data class ShowMessage(
        val message: String
    ) : ClassOverviewEffect
}
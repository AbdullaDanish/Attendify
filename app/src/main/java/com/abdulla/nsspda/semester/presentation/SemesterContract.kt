package com.abdulla.nsspda.semester.presentation

import com.abdulla.nsspda.semester.data.Semester

data class SemesterUiState(
    val semesters: List<Semester> = emptyList(),
    val isLoading: Boolean = true,
    val isAddDialogVisible: Boolean = false,
    val semesterToDelete: Semester? = null,
    val isSubmitting: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && semesters.isEmpty()
}

sealed interface SemesterIntent {

    data object AddClassClicked : SemesterIntent

    data object AddDialogDismissed : SemesterIntent

    data class AddSemesterSubmitted(
        val semester: String,
        val branch: String,
        val subject: String
    ) : SemesterIntent

    data class SemesterClicked(
        val semester: Semester
    ) : SemesterIntent

    data class DeleteClicked(
        val semester: Semester
    ) : SemesterIntent

    data object DeleteDismissed : SemesterIntent

    data object DeleteConfirmed : SemesterIntent
}

sealed interface SemesterEffect {

    data class NavigateToAttendance(
        val semester: String,
        val branch: String,
        val subject: String
    ) : SemesterEffect

    data class ShowMessage(
        val message: String
    ) : SemesterEffect
}
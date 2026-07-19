package com.abdulla.nsspda.attendance.presentation.marking

import java.time.LocalDate

enum class AttendanceMarkingMode {
    MARK_ABSENTEES,
    MARK_PRESENTEES
}

data class AttendanceStudentItem(
    val studentId: Long,
    val studentName: String,
    val usn: String,
    val isPresent: Boolean
)

data class AttendanceMarkingUiState(
    val semester: String = "",
    val branch: String = "",
    val subject: String = "",

    val selectedDate: LocalDate = LocalDate.now(),
    val markingMode: AttendanceMarkingMode =
        AttendanceMarkingMode.MARK_ABSENTEES,

    val students: List<AttendanceStudentItem> = emptyList(),

    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    val hasExistingAttendance: Boolean = false,
    val originalAttendance: Map<String, Boolean> = emptyMap(),

    val pendingDate: LocalDate? = null,
    val showDiscardChangesDialog: Boolean = false,
    val showOverwriteDialog: Boolean = false,
    val showExitConfirmationDialog: Boolean = false,

    val errorMessage: String? = null
) {
    val isClassValid: Boolean
        get() {
            return semester.isNotBlank() &&
                    branch.isNotBlank() &&
                    subject.isNotBlank()
        }

    val presentCount: Int
        get() = students.count { it.isPresent }

    val absentCount: Int
        get() = students.size - presentCount

    val totalCount: Int
        get() = students.size

    val currentAttendance: Map<String, Boolean>
        get() = students.associate {
            it.usn to it.isPresent
        }

    val hasUnsavedChanges: Boolean
        get() {
            if (students.isEmpty()) {
                return false
            }

            return currentAttendance != originalAttendance
        }

    val canSave: Boolean
        get() {
            return isClassValid &&
                    students.isNotEmpty() &&
                    !isLoading &&
                    !isSaving &&
                    hasUnsavedChanges
        }

    val isEmpty: Boolean
        get() = !isLoading &&
                errorMessage == null &&
                students.isEmpty()
}

sealed interface AttendanceMarkingIntent {

    data class StudentAttendanceMarkingToggled(
        val usn: String
    ) : AttendanceMarkingIntent

    data class DateSelected(
        val date: LocalDate
    ) : AttendanceMarkingIntent

    data class MarkingModeChanged(
        val mode: AttendanceMarkingMode
    ) : AttendanceMarkingIntent

    data object MarkAllPresent :
        AttendanceMarkingIntent

    data object MarkAllAbsent :
        AttendanceMarkingIntent

    data object SaveClicked :
        AttendanceMarkingIntent

    data object OverwriteConfirmed :
        AttendanceMarkingIntent

    data object OverwriteDismissed :
        AttendanceMarkingIntent

    data object DiscardChangesConfirmed :
        AttendanceMarkingIntent

    data object DiscardChangesDismissed :
        AttendanceMarkingIntent

    data object BackClicked :
        AttendanceMarkingIntent

    data object ExitConfirmed :
        AttendanceMarkingIntent

    data object ExitDismissed :
        AttendanceMarkingIntent

    data object RetryClicked :
        AttendanceMarkingIntent

    data object ErrorDismissed :
        AttendanceMarkingIntent
}

sealed interface AttendanceMarkingEffect {

    data class ShowMessage(
        val message: String
    ) : AttendanceMarkingEffect

    data object NavigateBack :
        AttendanceMarkingEffect
}
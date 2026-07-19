package com.abdulla.nsspda.attendance.presentation.details

import java.time.LocalDate

data class AttendanceDetailsItem(
    val id: Long,
    val studentName: String,
    val usn: String,
    val isPresent: Boolean
)

data class AttendanceDetailsUiState(
    val semester: String = "",
    val branch: String = "",
    val subject: String = "",
    val date: LocalDate? = null,

    val attendance: List<AttendanceDetailsItem> = emptyList(),
    val originalAttendance: Map<String, Boolean> = emptyMap(),

    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isExporting: Boolean = false,

    val showDeleteConfirmation: Boolean = false,
    val showDiscardConfirmation: Boolean = false,

    val errorMessage: String? = null,
    val navigateAfterDiscard: Boolean = false,
) {
    val isClassValid: Boolean
        get() {
            return semester.isNotBlank() &&
                    branch.isNotBlank() &&
                    subject.isNotBlank() &&
                    date != null
        }

    val totalCount: Int
        get() = attendance.size

    val presentCount: Int
        get() = attendance.count { it.isPresent }

    val absentCount: Int
        get() = totalCount - presentCount

    val currentAttendance: Map<String, Boolean>
        get() = attendance.associate {
            it.usn.normalizedUsn() to it.isPresent
        }

    val hasUnsavedChanges: Boolean
        get() {
            return isEditing &&
                    currentAttendance != originalAttendance
        }

    val isBusy: Boolean
        get() {
            return isLoading ||
                    isSaving ||
                    isDeleting ||
                    isExporting
        }

    val isEmpty: Boolean
        get() {
            return !isLoading &&
                    errorMessage == null &&
                    attendance.isEmpty()
        }

    val canSave: Boolean
        get() {
            return isEditing &&
                    hasUnsavedChanges &&
                    attendance.isNotEmpty() &&
                    !isBusy
        }
}

sealed interface AttendanceDetailsIntent {

    data object BackClicked :
        AttendanceDetailsIntent

    data object EditClicked :
        AttendanceDetailsIntent

    data object CancelEditingClicked :
        AttendanceDetailsIntent

    data class StudentAttendanceToggled(
        val usn: String
    ) : AttendanceDetailsIntent

    data object MarkAllPresent :
        AttendanceDetailsIntent

    data object MarkAllAbsent :
        AttendanceDetailsIntent

    data object SaveClicked :
        AttendanceDetailsIntent

    data object DeleteClicked :
        AttendanceDetailsIntent

    data object DeleteConfirmed :
        AttendanceDetailsIntent

    data object DeleteDismissed :
        AttendanceDetailsIntent

    data object DiscardConfirmed :
        AttendanceDetailsIntent

    data object DiscardDismissed :
        AttendanceDetailsIntent

    data object ExportClicked :
        AttendanceDetailsIntent

    data object RetryClicked :
        AttendanceDetailsIntent
}

sealed interface AttendanceDetailsEffect {

    data object NavigateBack :
        AttendanceDetailsEffect

    data class ShowMessage(
        val message: String
    ) : AttendanceDetailsEffect

    data class ShareExportedFile(
        val uri: android.net.Uri,
        val mimeType: String,
        val fileName: String
    ) : AttendanceDetailsEffect
}

private fun String.normalizedUsn(): String {
    return trim().uppercase()
}
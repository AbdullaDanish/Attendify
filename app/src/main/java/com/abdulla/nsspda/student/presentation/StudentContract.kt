package com.abdulla.nsspda.student.presentation

import android.net.Uri
import com.abdulla.nsspda.student.data.imports.HeaderDetectionResult
import com.abdulla.nsspda.student.data.imports.StudentImportPreview
import com.abdulla.nsspda.student.data.local.Student
import com.abdulla.nsspda.student.domain.StudentBulkImportResult

data class StudentUiState(
    val semester: String = "",
    val branch: String = "",
    val subject: String = "",
    val students: List<Student> = emptyList(),

    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,

    val isAddDialogVisible: Boolean = false,
    val studentToDelete: Student? = null,
    val isDeleteAllDialogVisible: Boolean = false,

    val screenError: String? = null,

    val importState: StudentImportUiState =
        StudentImportUiState.Idle
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
                    screenError == null &&
                    students.isEmpty()
        }

    val isImportInProgress: Boolean
        get() {
            return importState is
                    StudentImportUiState.Analyzing ||
                    importState is
                            StudentImportUiState.CreatingPreview ||
                    importState is
                            StudentImportUiState.Importing
        }

}

sealed interface StudentImportUiState {

    data object Idle : StudentImportUiState

    data object RequestFileSelection :
        StudentImportUiState

    data class Analyzing(
        val fileName: String? = null
    ) : StudentImportUiState

    data class MappingRequired(
        val detections: List<HeaderDetectionResult>,
        val selectedDetectionIndex: Int,
        val selectedNameColumnIndex: Int?,
        val selectedUsnColumnIndex: Int?
    ) : StudentImportUiState {

        val selectedDetection: HeaderDetectionResult?
            get() = detections.getOrNull(
                selectedDetectionIndex
            )

        val canCreatePreview: Boolean
            get() {
                return selectedNameColumnIndex != null &&
                        selectedUsnColumnIndex != null &&
                        selectedNameColumnIndex !=
                        selectedUsnColumnIndex
            }
    }

    data object CreatingPreview :
        StudentImportUiState

    data class PreviewReady(
        val preview: StudentImportPreview
    ) : StudentImportUiState

    data object Importing :
        StudentImportUiState

    data class Completed(
        val preview: StudentImportPreview,
        val databaseResult:
        StudentBulkImportResult
    ) : StudentImportUiState

    data class Error(
        val message: String
    ) : StudentImportUiState
}

sealed interface StudentIntent {

    data object AddStudentClicked : StudentIntent

    data object AddStudentDismissed : StudentIntent

    data class AddStudentSubmitted(
        val studentName: String,
        val usn: String
    ) : StudentIntent

    data class DeleteStudentClicked(
        val student: Student
    ) : StudentIntent

    data object DeleteStudentDismissed :
        StudentIntent

    data object DeleteStudentConfirmed :
        StudentIntent

    data object DeleteAllClicked :
        StudentIntent

    data object DeleteAllDismissed :
        StudentIntent

    data object DeleteAllConfirmed :
        StudentIntent

    data object RetryClicked :
        StudentIntent

    data object ImportStudentsClicked :
        StudentIntent

    data class ImportFileSelected(
        val uri: Uri
    ) : StudentIntent

    data object ImportFileSelectionCancelled :
        StudentIntent

    data class ImportSheetSelected(
        val detectionIndex: Int
    ) : StudentIntent

    data class ImportNameColumnSelected(
        val columnIndex: Int
    ) : StudentIntent

    data class ImportUsnColumnSelected(
        val columnIndex: Int
    ) : StudentIntent

    data object ImportMappingConfirmed :
        StudentIntent

    data object ImportPreviewConfirmed :
        StudentIntent

    data object ImportPreviewDismissed :
        StudentIntent

    data object ImportResultDismissed :
        StudentIntent

    data object ImportErrorDismissed :
        StudentIntent

    data object ImportCancelled :
        StudentIntent
}

sealed interface StudentEffect {

    data class ShowMessage(
        val message: String
    ) : StudentEffect

    data object LaunchExcelFilePicker :
        StudentEffect
}
package com.abdulla.nsspda.student.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.student.data.imports.StudentImportMapping
import com.abdulla.nsspda.student.data.local.Student
import com.abdulla.nsspda.student.domain.AddStudentResult
import com.abdulla.nsspda.student.domain.StudentImportRepository
import com.abdulla.nsspda.student.domain.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repository: StudentRepository,
    private val importRepository:
    StudentImportRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var selectedImportUri: Uri? = null
    private val semester = Uri.decode(
        savedStateHandle.get<String>("semester").orEmpty()
    )

    private val branch = Uri.decode(
        savedStateHandle.get<String>("branch").orEmpty()
    )

    private val subject = Uri.decode(
        savedStateHandle.get<String>("subject").orEmpty()
    )

    private val _uiState =
        MutableStateFlow(
            StudentUiState(
                semester = semester,
                branch = branch,
                subject = subject
            )
        )

    val uiState: StateFlow<StudentUiState> =
        _uiState

    private val effectChannel =
        Channel<StudentEffect>(
            capacity = Channel.BUFFERED
        )

    val effects =
        effectChannel.receiveAsFlow()

    private var observeStudentsJob: Job? = null

    init {
        observeStudents()
    }

    fun onIntent(intent: StudentIntent) {
        when (intent) {
            StudentIntent.AddStudentClicked -> {
                showAddStudentDialog()
            }

            StudentIntent.AddStudentDismissed -> {
                dismissAddStudentDialog()
            }

            is StudentIntent.AddStudentSubmitted -> {
                addStudent(
                    studentName = intent.studentName,
                    usn = intent.usn
                )
            }

            is StudentIntent.DeleteStudentClicked -> {
                requestStudentDeletion(
                    student = intent.student
                )
            }

            StudentIntent.DeleteStudentDismissed -> {
                dismissStudentDeletion()
            }

            StudentIntent.DeleteStudentConfirmed -> {
                deleteSelectedStudent()
            }

            StudentIntent.DeleteAllClicked -> {
                requestDeleteAll()
            }

            StudentIntent.DeleteAllDismissed -> {
                dismissDeleteAll()
            }

            StudentIntent.DeleteAllConfirmed -> {
                deleteAllStudents()
            }

            StudentIntent.RetryClicked -> {
                observeStudents()
            }
            StudentIntent.ImportStudentsClicked -> {
                requestImportFile()
            }

            is StudentIntent.ImportFileSelected -> {
                analyzeImportFile(intent.uri)
            }

            StudentIntent.ImportFileSelectionCancelled -> {
                resetImport()
            }

            is StudentIntent.ImportSheetSelected -> {
                selectImportDetection(
                    intent.detectionIndex
                )
            }

            is StudentIntent.ImportNameColumnSelected -> {
                selectNameColumn(
                    intent.columnIndex
                )
            }

            is StudentIntent.ImportUsnColumnSelected -> {
                selectUsnColumn(
                    intent.columnIndex
                )
            }

            StudentIntent.ImportMappingConfirmed -> {
                createImportPreview()
            }

            StudentIntent.ImportPreviewConfirmed -> {
                confirmStudentImport()
            }
            StudentIntent.SelectionModeStarted -> {
                startSelectionMode()
            }

            StudentIntent.SelectionModeCancelled -> {
                cancelSelectionMode()
            }

            is StudentIntent.StudentSelectionToggled -> {
                toggleStudentSelection(
                    studentId = intent.studentId
                )
            }

            StudentIntent.SelectAllStudents -> {
                selectAllStudents()
            }

            StudentIntent.ClearStudentSelection -> {
                clearStudentSelection()
            }

            StudentIntent.DeleteSelectedClicked -> {
                requestDeleteSelected()
            }

            StudentIntent.DeleteSelectedConfirmed -> {
                deleteSelectedStudents()
            }

            StudentIntent.DeleteSelectedDismissed -> {
                dismissDeleteSelected()
            }
            StudentIntent.ImportPreviewDismissed,
            StudentIntent.ImportResultDismissed,
            StudentIntent.ImportErrorDismissed,
            StudentIntent.ImportCancelled -> {
                resetImport()
            }
        }
    }

    private fun observeStudents() {
        if (!_uiState.value.isClassValid) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    screenError =
                        "The class information is incomplete."
                )
            }

            return
        }

        observeStudentsJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                screenError = null
            )
        }

        observeStudentsJob =
            viewModelScope.launch {
                repository.observeStudents(
                    semester = semester,
                    branch = branch,
                    subject = subject
                )
                    .catch {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                screenError =
                                    "Unable to load students."
                            )
                        }
                    }
                    .collect { students ->
                        _uiState.update { state ->
                            val availableIds = students
                                .map(Student::id)
                                .toSet()

                            val validSelection =
                                state.selectedStudentIds
                                    .intersect(availableIds)

                            state.copy(
                                students = students,
                                selectedStudentIds = validSelection,
                                isSelectionMode =
                                    state.isSelectionMode &&
                                            students.isNotEmpty(),
                                isLoading = false,
                                screenError = null
                            )
                        }
                    }
            }
    }

    private fun showAddStudentDialog() {
        if (!_uiState.value.isClassValid) {
            showMessage(
                "Cannot add a student because class information is missing."
            )

            return
        }

        _uiState.update {
            it.copy(
                isAddDialogVisible = true
            )
        }
    }

    private fun dismissAddStudentDialog() {
        if (_uiState.value.isSubmitting) {
            return
        }

        _uiState.update {
            it.copy(
                isAddDialogVisible = false
            )
        }
    }

    private fun addStudent(
        studentName: String,
        usn: String
    ) {
        val normalizedName =
            normalizeStudentName(studentName)

        val normalizedUsn =
            normalizeUsn(usn)

        when {
            normalizedName.isBlank() -> {
                showMessage(
                    "Student name cannot be empty."
                )
                return
            }

            normalizedUsn.isBlank() -> {
                showMessage(
                    "USN cannot be empty."
                )
                return
            }

            !_uiState.value.isClassValid -> {
                showMessage(
                    "Class information is incomplete."
                )
                return
            }

            _uiState.value.isSubmitting -> {
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true
                )
            }

            try {
                val result =
                    repository.addStudent(
                        Student(
                            studentName =
                                normalizedName,
                            usn = normalizedUsn,
                            semester = semester,
                            branch = branch,
                            subject = subject
                        )
                    )

                when (result) {
                    AddStudentResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                isAddDialogVisible = false
                            )
                        }

                        effectChannel.send(
                            StudentEffect.ShowMessage(
                                "Student added successfully."
                            )
                        )
                    }

                    AddStudentResult.Duplicate -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false
                            )
                        }

                        effectChannel.send(
                            StudentEffect.ShowMessage(
                                "A student with this USN already exists in this class."
                            )
                        )
                    }
                }
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false
                    )
                }

                effectChannel.send(
                    StudentEffect.ShowMessage(
                        "Unable to add the student."
                    )
                )
            }
        }
    }

    private fun requestStudentDeletion(
        student: Student
    ) {
        _uiState.update {
            it.copy(
                studentToDelete = student
            )
        }
    }

    private fun dismissStudentDeletion() {
        if (_uiState.value.isSubmitting) {
            return
        }

        _uiState.update {
            it.copy(
                studentToDelete = null
            )
        }
    }

    private fun deleteSelectedStudent() {
        val student =
            _uiState.value.studentToDelete
                ?: return

        if (_uiState.value.isSubmitting) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true
                )
            }

            try {
                val deleted =
                    repository.deleteStudent(student)

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        studentToDelete = null
                    )
                }

                effectChannel.send(
                    StudentEffect.ShowMessage(
                        if (deleted) {
                            "Student deleted successfully."
                        } else {
                            "The student could not be found."
                        }
                    )
                )
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false
                    )
                }

                effectChannel.send(
                    StudentEffect.ShowMessage(
                        "Unable to delete the student."
                    )
                )
            }
        }
    }

    private fun requestDeleteAll() {
        if (_uiState.value.students.isEmpty()) {
            showMessage(
                "There are no students to delete."
            )

            return
        }

        _uiState.update {
            it.copy(
                isDeleteAllDialogVisible = true
            )
        }
    }

    private fun dismissDeleteAll() {
        if (_uiState.value.isSubmitting) {
            return
        }

        _uiState.update {
            it.copy(
                isDeleteAllDialogVisible = false
            )
        }
    }

    private fun deleteAllStudents() {
        if (
            _uiState.value.isSubmitting ||
            !_uiState.value.isClassValid
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true
                )
            }

            try {
                val deletedCount =
                    repository.deleteAllStudents(
                        semester = semester,
                        branch = branch,
                        subject = subject
                    )

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isDeleteAllDialogVisible = false
                    )
                }

                effectChannel.send(
                    StudentEffect.ShowMessage(
                        if (deletedCount > 0) {
                            "$deletedCount students deleted."
                        } else {
                            "No students were found."
                        }
                    )
                )
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false
                    )
                }

                effectChannel.send(
                    StudentEffect.ShowMessage(
                        "Unable to delete the students."
                    )
                )
            }
        }
    }

    private fun showMessage(
        message: String
    ) {
        viewModelScope.launch {
            effectChannel.send(
                StudentEffect.ShowMessage(
                    message = message
                )
            )
        }
    }

    private fun normalizeUsn(
        value: String
    ): String {
        return value
            .trim()
            .uppercase()
            .filterNot { character ->
                character.isWhitespace()
            }
    }

    private fun normalizeStudentName(
        value: String
    ): String {
        return value
            .trim()
            .split(
                regex = Regex("\\s+")
            )
            .joinToString(separator = " ")
    }

    private fun requestImportFile() {
        if (!_uiState.value.isClassValid) {
            showMessage(
                "Class information is incomplete."
            )
            return
        }

        if (_uiState.value.isImportInProgress) {
            return
        }

        _uiState.update {
            it.copy(
                importState =
                    StudentImportUiState
                        .RequestFileSelection
            )
        }

        viewModelScope.launch {
            effectChannel.send(
                StudentEffect.LaunchExcelFilePicker
            )
        }
    }

    private fun analyzeImportFile(
        uri: Uri
    ) {
        if (_uiState.value.isImportInProgress) {
            return
        }

        selectedImportUri = uri

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    importState =
                        StudentImportUiState.Analyzing()
                )
            }

            try {
                val detections =
                    importRepository
                        .analyzeWorkbook(uri)
                        .getOrThrow()

                if (detections.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            importState =
                                StudentImportUiState.Error(
                                    message =
                                        "No usable student table was detected in the workbook."
                                )
                        )
                    }

                    return@launch
                }

                val initialDetection =
                    detections.first()

                _uiState.update {
                    it.copy(
                        importState =
                            StudentImportUiState
                                .MappingRequired(
                                    detections =
                                        detections,
                                    selectedDetectionIndex =
                                        0,
                                    selectedNameColumnIndex =
                                        initialDetection
                                            .nameColumn
                                            ?.columnIndex,
                                    selectedUsnColumnIndex =
                                        initialDetection
                                            .usnColumn
                                            ?.columnIndex
                                )
                    )
                }
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        importState =
                            StudentImportUiState.Error(
                                message =
                                    exception.toUserImportMessage()
                            )
                    )
                }
            }
        }
    }

    private fun selectImportDetection(
        detectionIndex: Int
    ) {
        val state = _uiState.value.importState
                as? StudentImportUiState.MappingRequired
            ?: return

        val detection =
            state.detections.getOrNull(
                detectionIndex
            ) ?: return

        _uiState.update {
            it.copy(
                importState =
                    state.copy(
                        selectedDetectionIndex =
                            detectionIndex,
                        selectedNameColumnIndex =
                            detection.nameColumn
                                ?.columnIndex,
                        selectedUsnColumnIndex =
                            detection.usnColumn
                                ?.columnIndex
                    )
            )
        }
    }

    private fun selectNameColumn(
        columnIndex: Int
    ) {
        val state = _uiState.value.importState
                as? StudentImportUiState.MappingRequired
            ?: return

        _uiState.update {
            it.copy(
                importState =
                    state.copy(
                        selectedNameColumnIndex =
                            columnIndex
                    )
            )
        }
    }

    private fun selectUsnColumn(
        columnIndex: Int
    ) {
        val state = _uiState.value.importState
                as? StudentImportUiState.MappingRequired
            ?: return

        _uiState.update {
            it.copy(
                importState =
                    state.copy(
                        selectedUsnColumnIndex =
                            columnIndex
                    )
            )
        }
    }

    private fun createImportPreview() {
        val uri = selectedImportUri
            ?: run {
                showImportError(
                    "The selected file is no longer available."
                )
                return
            }

        val state = _uiState.value.importState
                as? StudentImportUiState.MappingRequired
            ?: return

        val detection =
            state.selectedDetection
                ?: return

        val nameColumn =
            state.selectedNameColumnIndex
                ?: return

        val usnColumn =
            state.selectedUsnColumnIndex
                ?: return

        if (nameColumn == usnColumn) {
            showImportError(
                "Student name and USN must use different columns."
            )
            return
        }

        val mapping = StudentImportMapping(
            sheetIndex = detection.sheetIndex,
            headerRowIndex = detection.headerRowIndex,
            nameColumnIndex = nameColumn,
            usnColumnIndex = usnColumn,
            detectionConfidence = detection.confidence
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    importState =
                        StudentImportUiState
                            .CreatingPreview
                )
            }

            try {
                val preview =
                    importRepository
                        .createPreview(
                            uri = uri,
                            mapping = mapping
                        )
                        .getOrThrow()

                if (preview.validRows.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            importState =
                                StudentImportUiState.Error(
                                    message =
                                        "No valid students were found using the selected columns."
                                )
                        )
                    }

                    return@launch
                }

                _uiState.update {
                    it.copy(
                        importState =
                            StudentImportUiState
                                .PreviewReady(
                                    preview = preview
                                )
                    )
                }
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                showImportError(
                    exception.toUserImportMessage()
                )
            }
        }
    }

    private fun confirmStudentImport() {
        val state = _uiState.value.importState
                as? StudentImportUiState.PreviewReady
            ?: return

        if (!_uiState.value.isClassValid) {
            showImportError(
                "Class information is incomplete."
            )
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    importState =
                        StudentImportUiState.Importing
                )
            }

            try {
                val students =
                    state.preview.validRows.map {
                            parsedRow ->
                        Student(
                            studentName =
                                parsedRow.studentName,
                            usn = parsedRow.usn,
                            semester = semester,
                            branch = branch,
                            subject = subject
                        )
                    }

                val result =
                    repository.importStudents(
                        students = students
                    )

                _uiState.update {
                    it.copy(
                        importState =
                            StudentImportUiState.Completed(
                                preview = state.preview,
                                databaseResult = result
                            )
                    )
                }
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                showImportError(
                    "Unable to save the imported students."
                )
            }
        }
    }

    private fun resetImport() {
        selectedImportUri = null

        _uiState.update {
            it.copy(
                importState =
                    StudentImportUiState.Idle
            )
        }
    }

    private fun showImportError(
        message: String
    ) {
        _uiState.update {
            it.copy(
                importState =
                    StudentImportUiState.Error(
                        message = message
                    )
            )
        }
    }

    private fun Throwable.toUserImportMessage():
            String {
        val rawMessage =
            message.orEmpty().lowercase()

        return when {
            rawMessage.contains("password") ||
                    rawMessage.contains("encrypted") -> {
                "Password-protected Excel files are not supported."
            }

            rawMessage.contains("larger than") -> {
                "The selected workbook is too large. Please use a file smaller than 25 MB."
            }

            rawMessage.contains("unable to open") -> {
                "The selected file could not be opened."
            }

            rawMessage.contains("invalid") ||
                    rawMessage.contains("corrupt") -> {
                "The selected workbook appears to be invalid or corrupted."
            }

            else -> {
                "The workbook could not be analyzed. Please select a valid .xls or .xlsx file."
            }
        }
    }

    private fun startSelectionMode() {
        val state = _uiState.value

        if (
            state.students.isEmpty() ||
            state.isSubmitting
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isSelectionMode = true,
                selectedStudentIds = emptySet()
            )
        }
    }

    private fun cancelSelectionMode() {
        if (_uiState.value.isSubmitting) {
            return
        }

        _uiState.update {
            it.copy(
                isSelectionMode = false,
                selectedStudentIds = emptySet(),
                isDeleteSelectedDialogVisible = false
            )
        }
    }

    private fun toggleStudentSelection(
        studentId: Long
    ) {
        val state = _uiState.value

        if (
            !state.isSelectionMode ||
            state.isSubmitting
        ) {
            return
        }

        val studentExists =
            state.students.any { student ->
                student.id == studentId
            }

        if (!studentExists) {
            return
        }

        _uiState.update {
            val updatedSelection =
                if (studentId in it.selectedStudentIds) {
                    it.selectedStudentIds - studentId
                } else {
                    it.selectedStudentIds + studentId
                }

            it.copy(
                selectedStudentIds = updatedSelection
            )
        }
    }

    private fun selectAllStudents() {
        val state = _uiState.value

        if (
            !state.isSelectionMode ||
            state.isSubmitting
        ) {
            return
        }

        _uiState.update {
            it.copy(
                selectedStudentIds =
                    it.students
                        .map(Student::id)
                        .toSet()
            )
        }
    }

    private fun clearStudentSelection() {
        if (_uiState.value.isSubmitting) {
            return
        }

        _uiState.update {
            it.copy(
                selectedStudentIds = emptySet()
            )
        }
    }

    private fun requestDeleteSelected() {
        val state = _uiState.value

        if (
            !state.isSelectionMode ||
            !state.hasSelectedStudents ||
            state.isSubmitting
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isDeleteSelectedDialogVisible = true
            )
        }
    }

    private fun dismissDeleteSelected() {
        if (_uiState.value.isSubmitting) {
            return
        }

        _uiState.update {
            it.copy(
                isDeleteSelectedDialogVisible = false
            )
        }
    }

    private fun deleteSelectedStudents() {
        val state = _uiState.value

        if (
            state.isSubmitting ||
            !state.isSelectionMode ||
            state.selectedStudentIds.isEmpty()
        ) {
            return
        }

        val selectedIds =
            state.selectedStudentIds.toList()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true
                )
            }

            try {
                val deletedCount =
                    repository.deleteStudents(
                        studentIds = selectedIds
                    )

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSelectionMode = false,
                        selectedStudentIds = emptySet(),
                        isDeleteSelectedDialogVisible = false
                    )
                }

                effectChannel.send(
                    StudentEffect.ShowMessage(
                        message = when {
                            deletedCount == 0 ->
                                "No selected students were found."

                            deletedCount == 1 ->
                                "1 student deleted."

                            else ->
                                "$deletedCount students deleted."
                        }
                    )
                )
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isDeleteSelectedDialogVisible = false
                    )
                }

                effectChannel.send(
                    StudentEffect.ShowMessage(
                        "Unable to delete the selected students."
                    )
                )
            }
        }
    }
}
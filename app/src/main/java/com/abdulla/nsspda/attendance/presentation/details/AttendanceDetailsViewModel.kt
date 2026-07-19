package com.abdulla.nsspda.attendance.presentation.details

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.attendance.data.local.StudentAttendance
import com.abdulla.nsspda.attendance.data.local.toDatabaseDate
import com.abdulla.nsspda.attendance.domain.model.export.AttendanceExporter
import com.abdulla.nsspda.attendance.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AttendanceDetailsViewModel @Inject constructor(
    private val attendanceRepository:
    AttendanceRepository,
    private val attendanceExporter:
    AttendanceExporter,
    private val zoneId: ZoneId,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val semester = Uri.decode(
        savedStateHandle.get<String>("semester").orEmpty()
    )

    private val branch = Uri.decode(
        savedStateHandle.get<String>("branch").orEmpty()
    )

    private val subject = Uri.decode(
        savedStateHandle.get<String>("subject").orEmpty()
    )

    private val selectedDate: LocalDate? =
        savedStateHandle.get<String>("date")
            ?.let { dateValue ->
                runCatching {
                    LocalDate.parse(dateValue)
                }.getOrNull()
            }

    private val _uiState =
        MutableStateFlow(
            AttendanceDetailsUiState(
                semester = semester,
                branch = branch,
                subject = subject,
                date = selectedDate
            )
        )

    val uiState: StateFlow<AttendanceDetailsUiState> =
        _uiState.asStateFlow()

    private val effectChannel =
        Channel<AttendanceDetailsEffect>(
            capacity = Channel.BUFFERED
        )

    val effects =
        effectChannel.receiveAsFlow()

    private var attendanceJob: Job? = null

    init {
        observeAttendance()
    }

    fun onIntent(
        intent: AttendanceDetailsIntent
    ) {
        when (intent) {
            AttendanceDetailsIntent.BackClicked -> {
                requestExit()
            }

            AttendanceDetailsIntent.EditClicked -> {
                startEditing()
            }

            AttendanceDetailsIntent.CancelEditingClicked -> {
                requestCancelEditing()
            }

            is AttendanceDetailsIntent
            .StudentAttendanceToggled -> {
                toggleAttendance(
                    usn = intent.usn
                )
            }

            AttendanceDetailsIntent.MarkAllPresent -> {
                markAll(isPresent = true)
            }

            AttendanceDetailsIntent.MarkAllAbsent -> {
                markAll(isPresent = false)
            }

            AttendanceDetailsIntent.SaveClicked -> {
                saveChanges()
            }

            AttendanceDetailsIntent.DeleteClicked -> {
                _uiState.update {
                    it.copy(
                        showDeleteConfirmation = true
                    )
                }
            }

            AttendanceDetailsIntent.DeleteConfirmed -> {
                deleteAttendance()
            }

            AttendanceDetailsIntent.DeleteDismissed -> {
                _uiState.update {
                    it.copy(
                        showDeleteConfirmation = false
                    )
                }
            }

            AttendanceDetailsIntent.DiscardConfirmed -> {
                discardChangesAndExitEditing()
            }

            AttendanceDetailsIntent.DiscardDismissed -> {
                _uiState.update {
                    it.copy(
                        showDiscardConfirmation = false,
                        navigateAfterDiscard = false
                    )
                }
            }

            AttendanceDetailsIntent.ExportClicked -> {
                exportAttendance()
            }

            AttendanceDetailsIntent.RetryClicked -> {
                observeAttendance()
            }
        }
    }

    private fun observeAttendance() {
        attendanceJob?.cancel()

        val state = _uiState.value
        val date = state.date

        if (
            !state.isClassValid ||
            date == null
        ) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage =
                        "Attendance information is incomplete."
                )
            }
            return
        }

        attendanceJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            attendanceRepository
                .observeAttendanceForDate(
                    semester = semester,
                    branch = branch,
                    subject = subject,
                    date = date.toDatabaseDate(zoneId)
                )
                .catch { exception ->
                    if (exception is CancellationException) {
                        throw exception
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                "Unable to load attendance."
                        )
                    }
                }
                .collect { records ->
                    applyAttendanceRecords(records)
                }
        }
    }

    private fun applyAttendanceRecords(
        records: List<StudentAttendance>
    ) {
        val currentState = _uiState.value

        if (
            currentState.isEditing &&
            currentState.hasUnsavedChanges &&
            !currentState.isSaving
        ) {
            return
        }

        val items = records.map { attendance ->
            AttendanceDetailsItem(
                id = attendance.id,
                studentName = attendance.studentName,
                usn = attendance.usn,
                isPresent = attendance.present
            )
        }

        val originalSnapshot =
            items.associate {
                it.usn.trim().uppercase() to
                        it.isPresent
            }

        _uiState.update {
            it.copy(
                attendance = items,
                originalAttendance = originalSnapshot,
                isLoading = false,
                isEditing = false,
                isSaving = false,
                errorMessage = null
            )
        }
    }

    private fun startEditing() {
        val state = _uiState.value

        if (
            state.isBusy ||
            state.attendance.isEmpty()
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isEditing = true
            )
        }
    }

    private fun toggleAttendance(
        usn: String
    ) {
        val state = _uiState.value

        if (
            !state.isEditing ||
            state.isBusy
        ) {
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                attendance =
                    currentState.attendance.map { item ->
                        if (
                            item.usn.equals(
                                usn,
                                ignoreCase = true
                            )
                        ) {
                            item.copy(
                                isPresent = !item.isPresent
                            )
                        } else {
                            item
                        }
                    }
            )
        }
    }

    private fun markAll(
        isPresent: Boolean
    ) {
        val state = _uiState.value

        if (
            !state.isEditing ||
            state.isBusy ||
            state.attendance.isEmpty()
        ) {
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                attendance =
                    currentState.attendance.map {
                        it.copy(
                            isPresent = isPresent
                        )
                    }
            )
        }
    }

    private fun requestCancelEditing() {
        val state = _uiState.value

        if (!state.isEditing) {
            return
        }

        if (state.hasUnsavedChanges) {
            _uiState.update {
                it.copy(
                    showDiscardConfirmation = true,
                    navigateAfterDiscard = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isEditing = false
                )
            }
        }
    }

    private fun discardChangesAndExitEditing() {
        val state = _uiState.value

        val restoredItems =
            state.attendance.map { item ->
                val originalValue =
                    state.originalAttendance[
                        item.usn.normalizedUsn()
                    ] ?: item.isPresent

                item.copy(
                    isPresent = originalValue
                )
            }

        val shouldNavigate =
            state.navigateAfterDiscard

        _uiState.update {
            it.copy(
                attendance = restoredItems,
                isEditing = false,
                showDiscardConfirmation = false,
                navigateAfterDiscard = false
            )
        }

        if (shouldNavigate) {
            sendEffect(
                AttendanceDetailsEffect.NavigateBack
            )
        }
    }

    private fun saveChanges() {
        val state = _uiState.value
        val date = state.date

        if (
            !state.canSave ||
            date == null
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            try {
                val databaseDate =
                    date.toDatabaseDate(zoneId)

                val records =
                    state.attendance.map { item ->
                        StudentAttendance(
                            id = item.id,
                            studentName = item.studentName,
                            usn = item.usn,
                            semester = semester,
                            branch = branch,
                            subject = subject,
                            present = item.isPresent,
                            date = databaseDate
                        )
                    }

                val result =
                    attendanceRepository.saveAttendance(
                        attendanceList = records
                    )

                if (!result.wasSuccessful) {
                    throw IllegalStateException(
                        "Not all records were saved."
                    )
                }

                val savedSnapshot =
                    state.attendance.associate {
                        it.usn.normalizedUsn() to
                                it.isPresent
                    }

                _uiState.update {
                    it.copy(
                        originalAttendance = savedSnapshot,
                        isEditing = false,
                        isSaving = false,
                        errorMessage = null
                    )
                }

                sendEffect(
                    AttendanceDetailsEffect.ShowMessage(
                        "Attendance updated successfully."
                    )
                )
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage =
                            "Attendance could not be updated."
                    )
                }
            }
        }
    }

    private fun deleteAttendance() {
        val state = _uiState.value
        val date = state.date

        if (
            date == null ||
            state.isDeleting
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDeleting = true,
                    showDeleteConfirmation = false,
                    errorMessage = null
                )
            }

            try {
                val deletedCount =
                    attendanceRepository
                        .deleteAttendanceForDate(
                            semester = semester,
                            branch = branch,
                            subject = subject,
                            date = date.toDatabaseDate(
                                zoneId
                            )
                        )

                if (deletedCount <= 0) {
                    throw IllegalStateException(
                        "No attendance records were deleted."
                    )
                }

                sendEffect(
                    AttendanceDetailsEffect.ShowMessage(
                        "Attendance session deleted."
                    )
                )

                sendEffect(
                    AttendanceDetailsEffect.NavigateBack
                )
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage =
                            "Attendance could not be deleted."
                    )
                }
            }
        }
    }

    private fun requestExit() {
        val state = _uiState.value

        if (
            state.isEditing &&
            state.hasUnsavedChanges
        ) {
            _uiState.update {
                it.copy(
                    showDiscardConfirmation = true,
                    navigateAfterDiscard = true
                )
            }
        } else {
            sendEffect(
                AttendanceDetailsEffect.NavigateBack
            )
        }
    }

    private fun sendEffect(
        effect: AttendanceDetailsEffect
    ) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    private fun String.normalizedUsn(): String {
        return trim().uppercase()
    }

    private fun exportAttendance() {
        val state = _uiState.value
        val date = state.date

        if (
            state.isBusy ||
            date == null
        ) {
            return
        }

        if (state.attendance.isEmpty()) {
            sendEffect(
                AttendanceDetailsEffect.ShowMessage(
                    "There are no attendance records to export."
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    errorMessage = null
                )
            }

            try {
                val databaseDate =
                    date.toDatabaseDate(zoneId)

                val records =
                    state.attendance.map { item ->
                        StudentAttendance(
                            id = item.id,
                            studentName =
                                item.studentName,
                            usn = item.usn,
                            semester = semester,
                            branch = branch,
                            subject = subject,
                            present =
                                item.isPresent,
                            date = databaseDate
                        )
                    }

                val result =
                    attendanceExporter
                        .exportAttendance(
                            attendance = records,
                            semester = semester,
                            branch = branch,
                            subject = subject,
                            date = date
                        )

                _uiState.update {
                    it.copy(
                        isExporting = false
                    )
                }

                sendEffect(
                    AttendanceDetailsEffect
                        .ShareExportedFile(
                            uri = result.uri,
                            mimeType =
                                result.mimeType,
                            fileName =
                                result.fileName
                        )
                )
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false
                    )
                }

                sendEffect(
                    AttendanceDetailsEffect.ShowMessage(
                        exception.message
                            ?: "Attendance could not be exported."
                    )
                )
            }
        }
    }
}
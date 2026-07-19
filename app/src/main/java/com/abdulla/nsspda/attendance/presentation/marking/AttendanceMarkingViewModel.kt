package com.abdulla.nsspda.attendance.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.attendance.data.local.StudentAttendance
import com.abdulla.nsspda.attendance.data.local.toDatabaseDate
import com.abdulla.nsspda.attendance.domain.repository.AttendanceRepository
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingEffect
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingIntent
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingMode
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceStudentItem
import com.abdulla.nsspda.attendance.presentation.marking.AttendanceMarkingUiState
import com.abdulla.nsspda.student.data.local.Student
import com.abdulla.nsspda.student.domain.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AttendanceMarkingViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val clock: Clock,
    private val zoneId: ZoneId,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val semester = savedStateHandle.get<String>("semester").orEmpty()

    private val branch = savedStateHandle.get<String>("branch").orEmpty()

    private val subject = savedStateHandle.get<String>("subject").orEmpty()

    private val initialDate =
        LocalDate.now(clock)

    private val _uiState =
        MutableStateFlow(
            AttendanceMarkingUiState(
                semester = semester,
                branch = branch,
                subject = subject,
                selectedDate = initialDate
            )
        )

    val uiState: StateFlow<AttendanceMarkingUiState> =
        _uiState.asStateFlow()

    private val effectChannel =
        Channel<AttendanceMarkingEffect>(
            capacity = Channel.BUFFERED
        )

    val effects =
        effectChannel.receiveAsFlow()

    private var currentStudents:
            List<Student> = emptyList()

    private var attendanceLoadingJob:
            Job? = null

    init {
        observeStudents()
    }

    fun onIntent(
        intent: AttendanceMarkingIntent
    ) {
        when (intent) {
            is AttendanceMarkingIntent
            .StudentAttendanceMarkingToggled -> {
                toggleStudentAttendance(
                    intent.usn
                )
            }

            is AttendanceMarkingIntent.DateSelected -> {
                requestDateChange(
                    intent.date
                )
            }

            is AttendanceMarkingIntent
            .MarkingModeChanged -> {
                changeMarkingMode(
                    intent.mode
                )
            }

            AttendanceMarkingIntent.MarkAllPresent -> {
                markAll(
                    isPresent = true
                )
            }

            AttendanceMarkingIntent.MarkAllAbsent -> {
                markAll(
                    isPresent = false
                )
            }

            AttendanceMarkingIntent.SaveClicked -> {
                requestSave()
            }

            AttendanceMarkingIntent
                .OverwriteConfirmed -> {
                _uiState.update {
                    it.copy(
                        showOverwriteDialog = false
                    )
                }

                saveAttendance()
            }

            AttendanceMarkingIntent
                .OverwriteDismissed -> {
                _uiState.update {
                    it.copy(
                        showOverwriteDialog = false
                    )
                }
            }

            AttendanceMarkingIntent
                .DiscardChangesConfirmed -> {
                confirmDiscardAndChangeDate()
            }

            AttendanceMarkingIntent
                .DiscardChangesDismissed -> {
                _uiState.update {
                    it.copy(
                        pendingDate = null,
                        showDiscardChangesDialog =
                            false
                    )
                }
            }

            AttendanceMarkingIntent.BackClicked -> {
                requestExit()
            }

            AttendanceMarkingIntent.ExitConfirmed -> {
                _uiState.update {
                    it.copy(
                        showExitConfirmationDialog =
                            false
                    )
                }

                sendEffect(
                    AttendanceMarkingEffect.NavigateBack
                )
            }

            AttendanceMarkingIntent.ExitDismissed -> {
                _uiState.update {
                    it.copy(
                        showExitConfirmationDialog =
                            false
                    )
                }
            }

            AttendanceMarkingIntent.RetryClicked -> {
                loadAttendanceForDate(
                    _uiState.value.selectedDate
                )
            }

            AttendanceMarkingIntent.ErrorDismissed -> {
                _uiState.update {
                    it.copy(
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun observeStudents() {
        if (!_uiState.value.isClassValid) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage =
                        "Class information is incomplete."
                )
            }

            return
        }

        viewModelScope.launch {
            studentRepository.observeStudents(
                semester = semester,
                branch = branch,
                subject = subject
            ).collect { students ->
                currentStudents = students

                loadAttendanceForDate(
                    _uiState.value.selectedDate
                )
            }
        }
    }

    private fun loadAttendanceForDate(
        date: LocalDate
    ) {
        attendanceLoadingJob?.cancel()

        attendanceLoadingJob =
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                }

                try {
                    attendanceRepository
                        .observeAttendanceForDate(
                            semester = semester,
                            branch = branch,
                            subject = subject,
                            date = date.toDatabaseDate(
                                zoneId
                            )
                        )
                        .collect { records ->
                            applyLoadedAttendance(
                                date = date,
                                students =
                                    currentStudents,
                                attendance =
                                    records
                            )
                        }
                } catch (
                    exception:
                    CancellationException
                ) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                "Unable to load attendance."
                        )
                    }
                }
            }
    }

    private fun applyLoadedAttendance(
        date: LocalDate,
        students: List<Student>,
        attendance: List<StudentAttendance>
    ) {
        val attendanceByUsn =
            attendance.associateBy {
                it.usn.normalizeUsn()
            }

        val hasExistingAttendance =
            attendance.isNotEmpty()

        val currentMode =
            _uiState.value.markingMode

        val defaultAttendance =
            currentMode ==
                    AttendanceMarkingMode
                        .MARK_ABSENTEES

        val studentItems =
            students.map { student ->
                val existing =
                    attendanceByUsn[
                        student.usn
                            .normalizeUsn()
                    ]

                AttendanceStudentItem(
                    studentId = student.id,
                    studentName =
                        student.studentName,
                    usn = student.usn,
                    isPresent =
                        existing?.present
                            ?: defaultAttendance
                )
            }

        val originalSnapshot =
            studentItems.associate {
                it.usn to it.isPresent
            }

        _uiState.update {
            it.copy(
                selectedDate = date,
                students = studentItems,
                originalAttendance =
                    originalSnapshot,
                hasExistingAttendance =
                    hasExistingAttendance,
                isLoading = false,
                pendingDate = null,
                showDiscardChangesDialog =
                    false,
                errorMessage = null
            )
        }
    }

    private fun toggleStudentAttendance(
        usn: String
    ) {
        if (_uiState.value.isSaving) {
            return
        }

        _uiState.update { state ->
            state.copy(
                students =
                    state.students.map { student ->
                        if (
                            student.usn
                                .equals(
                                    usn,
                                    ignoreCase = true
                                )
                        ) {
                            student.copy(
                                isPresent =
                                    !student.isPresent
                            )
                        } else {
                            student
                        }
                    }
            )
        }
    }

    private fun markAll(
        isPresent: Boolean
    ) {
        if (
            _uiState.value.isSaving ||
            _uiState.value.students.isEmpty()
        ) {
            return
        }

        _uiState.update { state ->
            state.copy(
                students =
                    state.students.map {
                        it.copy(
                            isPresent = isPresent
                        )
                    }
            )
        }
    }

    private fun changeMarkingMode(
        mode: AttendanceMarkingMode
    ) {
        val state = _uiState.value

        if (
            state.markingMode == mode ||
            state.isSaving
        ) {
            return
        }

        /*
         * Changing mode does not destroy existing
         * selections. It changes how a fresh date is
         * initialized and how the UI explains the
         * marking workflow.
         */
        _uiState.update {
            it.copy(
                markingMode = mode
            )
        }
    }

    private fun requestDateChange(
        date: LocalDate
    ) {
        val state = _uiState.value

        if (
            date == state.selectedDate ||
            state.isSaving
        ) {
            return
        }

        if (date.isAfter(LocalDate.now(clock))) {
            sendEffect(
                AttendanceMarkingEffect.ShowMessage(
                    "Future dates cannot be selected."
                )
            )
            return
        }

        if (state.hasUnsavedChanges) {
            _uiState.update {
                it.copy(
                    pendingDate = date,
                    showDiscardChangesDialog = true
                )
            }
        } else {
            loadAttendanceForDate(date)
        }
    }

    private fun confirmDiscardAndChangeDate() {
        val pendingDate =
            _uiState.value.pendingDate
                ?: return

        _uiState.update {
            it.copy(
                pendingDate = null,
                showDiscardChangesDialog =
                    false
            )
        }

        loadAttendanceForDate(
            pendingDate
        )
    }

    private fun requestSave() {
        val state = _uiState.value

        when {
            state.isSaving -> Unit

            state.students.isEmpty() -> {
                sendEffect(
                    AttendanceMarkingEffect.ShowMessage(
                        "There are no students to mark."
                    )
                )
            }

            !state.hasUnsavedChanges -> {
                sendEffect(
                    AttendanceMarkingEffect.ShowMessage(
                        "No attendance changes to save."
                    )
                )
            }

            state.hasExistingAttendance -> {
                _uiState.update {
                    it.copy(
                        showOverwriteDialog = true
                    )
                }
            }

            else -> {
                saveAttendance()
            }
        }
    }

    private fun saveAttendance() {
        val state = _uiState.value

        if (
            state.students.isEmpty() ||
            state.isSaving
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
                    state.selectedDate
                        .toDatabaseDate(zoneId)

                val attendanceRecords =
                    state.students.map { student ->
                        StudentAttendance(
                            studentName =
                                student.studentName,
                            usn = student.usn,
                            semester = semester,
                            branch = branch,
                            subject = subject,
                            present =
                                student.isPresent,
                            date = databaseDate
                        )
                    }

                val result =
                    attendanceRepository
                        .saveAttendance(
                            attendanceRecords
                        )

                if (!result.wasSuccessful) {
                    throw IllegalStateException(
                        "Not all attendance records were saved."
                    )
                }

                val savedSnapshot =
                    state.students.associate {
                        it.usn to it.isPresent
                    }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        hasExistingAttendance = true,
                        originalAttendance =
                            savedSnapshot,
                        showOverwriteDialog = false
                    )
                }

                sendEffect(
                    AttendanceMarkingEffect.ShowMessage(
                        buildSaveMessage(
                            insertedCount =
                                result.insertedCount,
                            updatedCount =
                                result.updatedCount
                        )
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
                        showOverwriteDialog = false,
                        errorMessage =
                            "Attendance could not be saved. No partial attendance was committed."
                    )
                }
            }
        }
    }

    private fun requestExit() {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update {
                it.copy(
                    showExitConfirmationDialog =
                        true
                )
            }
        } else {
            sendEffect(
                AttendanceMarkingEffect.NavigateBack
            )
        }
    }

    private fun buildSaveMessage(
        insertedCount: Int,
        updatedCount: Int
    ): String {
        return when {
            insertedCount > 0 &&
                    updatedCount > 0 -> {
                "$insertedCount records added and $updatedCount records updated."
            }

            insertedCount > 0 -> {
                "Attendance saved for $insertedCount students."
            }

            updatedCount > 0 -> {
                "Attendance updated for $updatedCount students."
            }

            else -> {
                "Attendance is already up to date."
            }
        }
    }

    private fun sendEffect(
        effect: AttendanceMarkingEffect
    ) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    private fun String.normalizeUsn():
            String {
        return trim().uppercase()
    }
}
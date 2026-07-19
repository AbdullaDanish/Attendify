package com.abdulla.nsspda.classoverview.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.student.domain.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ClassOverviewViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
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

    private val _uiState =
        MutableStateFlow(
            ClassOverviewUiState(
                semester = semester,
                branch = branch,
                subject = subject
            )
        )

    val uiState: StateFlow<ClassOverviewUiState> =
        _uiState.asStateFlow()

    private val effectChannel =
        Channel<ClassOverviewEffect>(
            capacity = Channel.BUFFERED
        )

    val effects = effectChannel.receiveAsFlow()

    private var studentCountJob: Job? = null

    init {
        observeStudentCount()
    }

    fun onIntent(
        intent: ClassOverviewIntent
    ) {
        when (intent) {
            ClassOverviewIntent.BackClicked -> {
                sendEffect(
                    ClassOverviewEffect.NavigateBack
                )
            }

            ClassOverviewIntent.ManageStudentsClicked -> {
                navigateToStudents()
            }

            ClassOverviewIntent.AttendanceHistoryClicked -> {
                navigateToAttendanceHistory()
            }

            ClassOverviewIntent.RetryClicked -> {
                observeStudentCount()
            }
        }
    }

    private fun observeStudentCount() {
        studentCountJob?.cancel()

        if (
            semester.isBlank() ||
            branch.isBlank() ||
            subject.isBlank()
        ) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage =
                        "Class information is incomplete."
                )
            }
            return
        }

        studentCountJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            studentRepository.observeStudentCount(
                semester = semester,
                branch = branch,
                subject = subject
            )
                .catch { exception ->
                    if (exception is CancellationException) {
                        throw exception
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                "Unable to load the class information."
                        )
                    }
                }
                .collect { studentCount ->
                    _uiState.update {
                        it.copy(
                            studentCount =
                                studentCount.coerceAtLeast(0),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun navigateToStudents() {
        val state = _uiState.value

        if (!state.isClassValid) {
            sendEffect(
                ClassOverviewEffect.ShowMessage(
                    "Class information is incomplete."
                )
            )
            return
        }

        sendEffect(
            ClassOverviewEffect.NavigateToStudents(
                semester = semester,
                branch = branch,
                subject = subject
            )
        )
    }

    private fun navigateToAttendanceHistory() {
        val state = _uiState.value

        if (!state.isClassValid) {
            sendEffect(
                ClassOverviewEffect.ShowMessage(
                    "Class information is incomplete."
                )
            )
            return
        }

        if (!state.hasStudents) {
            sendEffect(
                ClassOverviewEffect.ShowMessage(
                    "Add at least one student before taking attendance."
                )
            )
            return
        }

        sendEffect(
            ClassOverviewEffect.NavigateToAttendanceHistory(
                semester = semester,
                branch = branch,
                subject = subject
            )
        )
    }

    private fun sendEffect(
        effect: ClassOverviewEffect
    ) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }
}
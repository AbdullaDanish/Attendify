package com.abdulla.nsspda.semester.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.semester.data.Semester
import com.abdulla.nsspda.semester.domain.AddSemesterResult
import com.abdulla.nsspda.semester.domain.SemesterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SemesterViewModel @Inject constructor(
    private val repository: SemesterRepository
) : ViewModel() {

    private val localState =
        MutableStateFlow(SemesterUiState())

    val uiState: StateFlow<SemesterUiState> =
        combine(
            repository.observeSemesters(),
            localState
        ) { semesters, state ->
            state.copy(
                semesters = semesters,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 5_000
            ),
            initialValue = SemesterUiState()
        )

    private val effectChannel =
        Channel<SemesterEffect>(
            capacity = Channel.BUFFERED
        )

    val effects = effectChannel.receiveAsFlow()

    fun onIntent(intent: SemesterIntent) {
        when (intent) {
            SemesterIntent.AddClassClicked -> {
                showAddDialog()
            }

            SemesterIntent.AddDialogDismissed -> {
                dismissAddDialog()
            }

            is SemesterIntent.AddSemesterSubmitted -> {
                addSemester(intent)
            }

            is SemesterIntent.SemesterClicked -> {
                navigateToAttendance(intent.semester)
            }

            is SemesterIntent.DeleteClicked -> {
                requestDelete(intent.semester)
            }

            SemesterIntent.DeleteDismissed -> {
                dismissDeleteDialog()
            }

            SemesterIntent.DeleteConfirmed -> {
                confirmDelete()
            }
        }
    }

    private fun showAddDialog() {
        localState.update {
            it.copy(isAddDialogVisible = true)
        }
    }

    private fun dismissAddDialog() {
        if (localState.value.isSubmitting) {
            return
        }

        localState.update {
            it.copy(isAddDialogVisible = false)
        }
    }

    private fun requestDelete(semester: Semester) {
        localState.update {
            it.copy(semesterToDelete = semester)
        }
    }

    private fun dismissDeleteDialog() {
        if (localState.value.isSubmitting) {
            return
        }

        localState.update {
            it.copy(semesterToDelete = null)
        }
    }

    private fun navigateToAttendance(
        semester: Semester
    ) {
        viewModelScope.launch {
            effectChannel.send(
                SemesterEffect.NavigateToAttendance(
                    semester = semester.semester,
                    branch = semester.branch,
                    subject = semester.subject
                )
            )
        }
    }

    private fun addSemester(
        intent: SemesterIntent.AddSemesterSubmitted
    ) {
        val semesterValue =
            intent.semester.trim().uppercase()

        val branchValue =
            intent.branch.trim().uppercase()

        val subjectValue =
            intent.subject.trim()

        when {
            semesterValue.isBlank() -> {
                showMessage("Please select a semester.")
                return
            }

            branchValue.isBlank() -> {
                showMessage("Please select a branch.")
                return
            }

            subjectValue.isBlank() -> {
                showMessage("Please select a subject.")
                return
            }

            localState.value.isSubmitting -> {
                return
            }
        }

        viewModelScope.launch {
            localState.update {
                it.copy(isSubmitting = true)
            }

            try {
                val result = repository.addSemester(
                    Semester(
                        semester = semesterValue,
                        branch = branchValue,
                        subject = subjectValue
                    )
                )

                when (result) {
                    AddSemesterResult.Success -> {
                        localState.update {
                            it.copy(
                                isSubmitting = false,
                                isAddDialogVisible = false
                            )
                        }

                        effectChannel.send(
                            SemesterEffect.ShowMessage(
                                "Class added successfully."
                            )
                        )
                    }

                    AddSemesterResult.Duplicate -> {
                        localState.update {
                            it.copy(isSubmitting = false)
                        }

                        effectChannel.send(
                            SemesterEffect.ShowMessage(
                                "This class already exists."
                            )
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                localState.update {
                    it.copy(isSubmitting = false)
                }

                effectChannel.send(
                    SemesterEffect.ShowMessage(
                        "Unable to add the class."
                    )
                )
            }
        }
    }

    private fun confirmDelete() {
        val semester =
            localState.value.semesterToDelete
                ?: return

        if (localState.value.isSubmitting) {
            return
        }

        viewModelScope.launch {
            localState.update {
                it.copy(isSubmitting = true)
            }

            try {
                val deleted =
                    repository.deleteSemester(semester)

                localState.update {
                    it.copy(
                        isSubmitting = false,
                        semesterToDelete = null
                    )
                }

                effectChannel.send(
                    SemesterEffect.ShowMessage(
                        if (deleted) {
                            "Class deleted successfully."
                        } else {
                            "The class could not be found."
                        }
                    )
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                localState.update {
                    it.copy(isSubmitting = false)
                }

                effectChannel.send(
                    SemesterEffect.ShowMessage(
                        "Unable to delete the class."
                    )
                )
            }
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            effectChannel.send(
                SemesterEffect.ShowMessage(message)
            )
        }
    }
}
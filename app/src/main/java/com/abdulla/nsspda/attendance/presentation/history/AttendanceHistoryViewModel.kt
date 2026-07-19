package com.abdulla.nsspda.attendance.presentation.history

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.attendance.data.local.toLocalDate
import com.abdulla.nsspda.attendance.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
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

    private var historyJob:
            kotlinx.coroutines.Job? = null
    private val _uiState =
        MutableStateFlow(
            AttendanceHistoryUiState(
                semester = semester,
                branch = branch,
                subject = subject
            )
        )

    val uiState: StateFlow<AttendanceHistoryUiState> =
        _uiState.asStateFlow()

    private val effectChannel =
        Channel<AttendanceHistoryEffect>(
            capacity = Channel.BUFFERED
        )

    val effects =
        effectChannel.receiveAsFlow()

    init {
        observeAttendanceHistory()
    }

    fun onIntent(
        intent: AttendanceHistoryIntent
    ) {
        when (intent) {
            AttendanceHistoryIntent.BackClicked -> {
                sendEffect(
                    AttendanceHistoryEffect.NavigateBack
                )
            }
            AttendanceHistoryIntent.SearchClicked -> {
                navigateToSearch()
            }
            AttendanceHistoryIntent
                .NewAttendanceClicked -> {
                navigateToNewAttendance()
            }

            AttendanceHistoryIntent
                .PercentageClicked -> {
                navigateToPercentage()
            }

            is AttendanceHistoryIntent
            .AttendanceSessionClicked -> {
                navigateToDetails(intent.date)
            }

            AttendanceHistoryIntent.RetryClicked -> {
                observeAttendanceHistory()
            }
        }
    }
    private fun navigateToSearch() {
        if (!_uiState.value.isClassValid) {
            return
        }

        sendEffect(
            AttendanceHistoryEffect.NavigateToSearch(
                semester = semester,
                branch = branch,
                subject = subject
            )
        )
    }
    private fun observeAttendanceHistory() {

        historyJob?.cancel()

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

        historyJob = viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                }

                try {
                    attendanceRepository
                        .observeAttendanceSummaries(
                            semester = semester,
                            branch = branch,
                            subject = subject
                        )
                        .catch { exception ->
                            if (
                                exception is
                                        CancellationException
                            ) {
                                throw exception
                            }

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage =
                                        "Unable to load attendance history."
                                )
                            }
                        }
                        .collect { summaries ->
                            val sessions = summaries.map { summary ->
                            AttendanceHistoryItem(
                                date = summary.date.toLocalDate(zoneId),
                                totalCount = summary.totalCount,
                                presentCount = summary.presentCount,
                                absentCount = summary.absentCount
                            )
                        }.distinctBy {
                                        it.date
                                    }
                                    .sortedByDescending {
                                        it.date
                                    }

                            _uiState.update {
                                it.copy(
                                    sessions = sessions,
                                    isLoading = false,
                                    errorMessage = null
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
                            isLoading = false,
                            errorMessage =
                                "Unable to load attendance history."
                        )
                    }
                }
            }
    }

    private fun navigateToNewAttendance() {
        if (!_uiState.value.isClassValid) {
            return
        }

        sendEffect(
            AttendanceHistoryEffect
                .NavigateToNewAttendance(
                    semester = semester,
                    branch = branch,
                    subject = subject
                )
        )
    }

    private fun navigateToPercentage() {
        if (!_uiState.value.isClassValid) {
            return
        }

        sendEffect(
            AttendanceHistoryEffect
                .NavigateToPercentage(
                    semester = semester,
                    branch = branch,
                    subject = subject
                )
        )
    }

    private fun navigateToDetails(
        date: java.time.LocalDate
    ) {
        if (!_uiState.value.isClassValid) {
            return
        }

        sendEffect(
            AttendanceHistoryEffect
                .NavigateToAttendanceDetails(
                    semester = semester,
                    branch = branch,
                    subject = subject,
                    date = date
                )
        )
    }

    private fun sendEffect(
        effect: AttendanceHistoryEffect
    ) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }
}
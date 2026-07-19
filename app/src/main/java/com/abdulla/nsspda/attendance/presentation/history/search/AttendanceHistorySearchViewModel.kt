package com.abdulla.nsspda.attendance.presentation.history.search

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.attendance.data.local.toLocalDate
import com.abdulla.nsspda.attendance.domain.repository.AttendanceRepository
import com.abdulla.nsspda.attendance.presentation.history.AttendanceHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
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
class AttendanceHistorySearchViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val zoneId: ZoneId,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val semester = Uri.decode(
        savedStateHandle
            .get<String>("semester")
            .orEmpty()
    )

    private val branch = Uri.decode(
        savedStateHandle
            .get<String>("branch")
            .orEmpty()
    )

    private val subject = Uri.decode(
        savedStateHandle
            .get<String>("subject")
            .orEmpty()
    )

    private val _uiState =
        MutableStateFlow(
            AttendanceHistorySearchUiState(
                semester = semester,
                branch = branch,
                subject = subject
            )
        )

    val uiState: StateFlow<AttendanceHistorySearchUiState> =
        _uiState.asStateFlow()

    private val effectChannel =
        Channel<AttendanceHistorySearchEffect>(
            capacity = Channel.BUFFERED
        )

    val effects =
        effectChannel.receiveAsFlow()

    private var historyJob: Job? = null

    init {
        observeHistory()
    }

    fun onIntent(
        intent: AttendanceHistorySearchIntent
    ) {
        when (intent) {
            is AttendanceHistorySearchIntent.QueryChanged -> {
                updateQuery(intent.query)
            }

            AttendanceHistorySearchIntent.QueryCleared -> {
                updateQuery("")
            }

            is AttendanceHistorySearchIntent.SessionClicked -> {
                navigateToDetails(intent.date)
            }

            AttendanceHistorySearchIntent.RetryClicked -> {
                observeHistory()
            }
        }
    }

    private fun updateQuery(
        query: String
    ) {
        _uiState.update {
            it.copy(
                query = query
            )
        }
    }

    private fun observeHistory() {
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

            attendanceRepository
                .observeAttendanceSummaries(
                    semester = semester,
                    branch = branch,
                    subject = subject
                )
                .catch { exception ->
                    if (
                        exception is CancellationException
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
                    val sessions = summaries
                        .map { summary ->
                            AttendanceHistoryItem(
                                date =
                                    summary.date.toLocalDate(
                                        zoneId
                                    ),
                                totalCount =
                                    summary.totalCount,
                                presentCount =
                                    summary.presentCount,
                                absentCount =
                                    summary.absentCount
                            )
                        }
                        .distinctBy { it.date }
                        .sortedByDescending { it.date }

                    _uiState.update {
                        it.copy(
                            sessions = sessions,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun navigateToDetails(
        date: java.time.LocalDate
    ) {
        if (!_uiState.value.isClassValid) {
            return
        }

        viewModelScope.launch {
            effectChannel.send(
                AttendanceHistorySearchEffect
                    .NavigateToDetails(
                        semester = semester,
                        branch = branch,
                        subject = subject,
                        date = date
                    )
            )
        }
    }
}
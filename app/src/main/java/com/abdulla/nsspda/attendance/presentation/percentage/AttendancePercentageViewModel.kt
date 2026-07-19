package com.abdulla.nsspda.attendance.presentation.percentage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulla.nsspda.attendance.domain.model.export.AttendanceAnalyticsExporter
import com.abdulla.nsspda.attendance.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AttendancePercentageViewModel @Inject constructor(
    private val attendanceRepository:
    AttendanceRepository,
    private val analyticsExporter:
    AttendanceAnalyticsExporter,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val semester = savedStateHandle.get<String>("semester").orEmpty()

    private val branch = savedStateHandle.get<String>("branch").orEmpty()

    private val subject = savedStateHandle.get<String>("subject").orEmpty()
    private val today =
        LocalDate.now(clock)

    private val defaultStartDate =
        today.withDayOfMonth(1)

    private val _uiState =
        MutableStateFlow(
            AttendancePercentageUiState(
                semester = semester,
                branch = branch,
                subject = subject,
                startDate = defaultStartDate,
                endDate = today
            )
        )

    val uiState:
            StateFlow<AttendancePercentageUiState> =
        _uiState.asStateFlow()

    private val effectChannel =
        Channel<AttendancePercentageEffect>(
            capacity = Channel.BUFFERED
        )

    val effects =
        effectChannel.receiveAsFlow()

    fun onIntent(
        intent: AttendancePercentageIntent
    ) {
        when (intent) {
            AttendancePercentageIntent.BackClicked -> {
                sendEffect(
                    AttendancePercentageEffect.NavigateBack
                )
            }

            AttendancePercentageIntent.StartDateClicked,
            AttendancePercentageIntent.EndDateClicked -> Unit

            is AttendancePercentageIntent
            .StartDateSelected -> {
                updateStartDate(intent.date)
            }

            is AttendancePercentageIntent
            .EndDateSelected -> {
                updateEndDate(intent.date)
            }

            AttendancePercentageIntent
                .CalculateClicked -> {
                calculateAnalytics()
            }

            is AttendancePercentageIntent.SearchChanged -> {
                _uiState.update {
                    it.copy(
                        searchQuery = intent.query
                    )
                }
            }

            is AttendancePercentageIntent
            .RiskFilterSelected -> {
                _uiState.update {
                    it.copy(
                        selectedRiskLevel =
                            intent.riskLevel
                    )
                }
            }

            is AttendancePercentageIntent.SortSelected -> {
                _uiState.update {
                    it.copy(
                        sort = intent.sort
                    )
                }
            }

            AttendancePercentageIntent.ExportClicked -> {
                exportAnalytics()
            }

            AttendancePercentageIntent.RetryClicked -> {
                calculateAnalytics()
            }

            AttendancePercentageIntent.ErrorDismissed -> {
                _uiState.update {
                    it.copy(
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun updateStartDate(
        date: LocalDate
    ) {
        if (date.isAfter(today)) {
            sendEffect(
                AttendancePercentageEffect.ShowMessage(
                    "Start date cannot be in the future."
                )
            )
            return
        }

        _uiState.update { state ->
            state.copy(
                startDate = date,
                analytics = null,
                hasCalculated = false,
                errorMessage = null
            )
        }
    }

    private fun updateEndDate(
        date: LocalDate
    ) {
        if (date.isAfter(today)) {
            sendEffect(
                AttendancePercentageEffect.ShowMessage(
                    "End date cannot be in the future."
                )
            )
            return
        }

        _uiState.update { state ->
            state.copy(
                endDate = date,
                analytics = null,
                hasCalculated = false,
                errorMessage = null
            )
        }
    }

    private fun calculateAnalytics() {
        val state = _uiState.value

        if (state.isLoading) {
            return
        }

        if (!state.isClassValid) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Class information is incomplete."
                )
            }
            return
        }

        if (!state.isDateRangeValid) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Start date must not be after end date."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val analytics =
                    attendanceRepository
                        .calculateAnalytics(
                            semester = semester,
                            branch = branch,
                            subject = subject,
                            startDate =
                                state.startDate,
                            endDate =
                                state.endDate
                        )

                _uiState.update {
                    it.copy(
                        analytics = analytics,
                        isLoading = false,
                        hasCalculated = true,
                        errorMessage = null
                    )
                }
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasCalculated = true,
                        errorMessage =
                            exception.message
                                ?: "Unable to calculate attendance analytics."
                    )
                }
            }
        }
    }

    private fun sendEffect(
        effect: AttendancePercentageEffect
    ) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }

    private fun exportAnalytics() {
        val state = _uiState.value
        val analytics = state.analytics

        if (
            state.isLoading ||
            state.isExporting
        ) {
            return
        }

        if (
            analytics == null ||
            !analytics.hasData
        ) {
            sendEffect(
                AttendancePercentageEffect.ShowMessage(
                    "There is no analytics data to export."
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
                val result =
                    analyticsExporter.exportAnalytics(
                        analytics = analytics,
                        semester = semester,
                        branch = branch,
                        subject = subject
                    )

                _uiState.update {
                    it.copy(
                        isExporting = false
                    )
                }

                sendEffect(
                    AttendancePercentageEffect
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
                    AttendancePercentageEffect.ShowMessage(
                        exception.message
                            ?: "Attendance analytics could not be exported."
                    )
                )
            }
        }
    }
}
package com.abdulla.nsspda.attendance.presentation.percentage

import com.abdulla.nsspda.attendance.domain.model.AttendanceAnalytics
import com.abdulla.nsspda.attendance.domain.model.AttendanceRiskLevel
import com.abdulla.nsspda.attendance.domain.model.StudentAttendanceAnalytics
import java.time.LocalDate

enum class AttendanceStudentSort {
    LOWEST_ATTENDANCE,
    HIGHEST_ATTENDANCE,
    NAME,
    USN
}

data class AttendancePercentageUiState(
    val semester: String = "",
    val branch: String = "",
    val subject: String = "",

    val startDate: LocalDate,
    val endDate: LocalDate,

    val analytics: AttendanceAnalytics? = null,

    val searchQuery: String = "",
    val selectedRiskLevel:
    AttendanceRiskLevel? = null,
    val sort: AttendanceStudentSort =
        AttendanceStudentSort.LOWEST_ATTENDANCE,

    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val hasCalculated: Boolean = false,

    val errorMessage: String? = null
) {
    val isClassValid: Boolean
        get() {
            return semester.isNotBlank() &&
                    branch.isNotBlank() &&
                    subject.isNotBlank()
        }

    val isDateRangeValid: Boolean
        get() = !startDate.isAfter(endDate)

    val canCalculate: Boolean
        get() {
            return isClassValid &&
                    isDateRangeValid &&
                    !isLoading &&
                    !isExporting
        }

    val hasData: Boolean
        get() = analytics?.hasData == true

    val filteredStudents: List<StudentAttendanceAnalytics>
        get() {
            val source =
                analytics?.students.orEmpty()

            val filtered =
                source.filter { student ->
                    val matchesSearch =
                        searchQuery.isBlank() ||
                                student.studentName
                                    .contains(
                                        searchQuery,
                                        ignoreCase = true
                                    ) ||
                                student.usn.contains(
                                    searchQuery,
                                    ignoreCase = true
                                )

                    val matchesRisk =
                        selectedRiskLevel == null ||
                                student.riskLevel ==
                                selectedRiskLevel

                    matchesSearch && matchesRisk
                }

            return when (sort) {
                AttendanceStudentSort
                    .LOWEST_ATTENDANCE -> {
                    filtered.sortedBy {
                        it.percentage
                    }
                }

                AttendanceStudentSort
                    .HIGHEST_ATTENDANCE -> {
                    filtered.sortedByDescending {
                        it.percentage
                    }
                }

                AttendanceStudentSort.NAME -> {
                    filtered.sortedBy {
                        it.studentName.lowercase()
                    }
                }

                AttendanceStudentSort.USN -> {
                    filtered.sortedBy {
                        it.usn
                    }
                }
            }
        }
}

sealed interface AttendancePercentageIntent {

    data object BackClicked :
        AttendancePercentageIntent

    data object StartDateClicked :
        AttendancePercentageIntent

    data object EndDateClicked :
        AttendancePercentageIntent

    data class StartDateSelected(
        val date: LocalDate
    ) : AttendancePercentageIntent

    data class EndDateSelected(
        val date: LocalDate
    ) : AttendancePercentageIntent

    data object CalculateClicked :
        AttendancePercentageIntent

    data class SearchChanged(
        val query: String
    ) : AttendancePercentageIntent

    data class RiskFilterSelected(
        val riskLevel: AttendanceRiskLevel?
    ) : AttendancePercentageIntent

    data class SortSelected(
        val sort: AttendanceStudentSort
    ) : AttendancePercentageIntent

    data object ExportClicked :
        AttendancePercentageIntent

    data object RetryClicked :
        AttendancePercentageIntent

    data object ErrorDismissed :
        AttendancePercentageIntent
}

sealed interface AttendancePercentageEffect {

    data object NavigateBack :
        AttendancePercentageEffect

    data class ShowMessage(
        val message: String
    ) : AttendancePercentageEffect

    data class ShareExportedFile(
        val uri: android.net.Uri,
        val mimeType: String,
        val fileName: String
    ) : AttendancePercentageEffect
}
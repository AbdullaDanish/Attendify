package com.abdulla.nsspda.attendance.presentation.history.search

import com.abdulla.nsspda.attendance.presentation.history.AttendanceHistoryItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AttendanceHistorySearchUiState(
    val semester: String = "",
    val branch: String = "",
    val subject: String = "",
    val query: String = "",
    val sessions: List<AttendanceHistoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isClassValid: Boolean
        get() =
            semester.isNotBlank() &&
                    branch.isNotBlank() &&
                    subject.isNotBlank()

    val filteredSessions: List<AttendanceHistoryItem>
        get() {
            val normalizedQuery =
                query.trim().lowercase(Locale.getDefault())

            if (normalizedQuery.isBlank()) {
                return sessions
            }

            return sessions.filter { session ->
                session.matchesSearchQuery(
                    query = normalizedQuery
                )
            }
        }

    val hasNoResults: Boolean
        get() =
            !isLoading &&
                    errorMessage == null &&
                    query.isNotBlank() &&
                    filteredSessions.isEmpty()
}

private fun AttendanceHistoryItem.matchesSearchQuery(
    query: String
): Boolean {
    val locale = Locale.getDefault()

    val searchableValues = listOf(
        date.toString(),
        date.dayOfMonth.toString(),
        date.monthValue.toString(),
        date.year.toString(),

        date.format(
            DateTimeFormatter.ofPattern(
                "EEEE",
                locale
            )
        ),

        date.format(
            DateTimeFormatter.ofPattern(
                "EEE",
                locale
            )
        ),

        date.format(
            DateTimeFormatter.ofPattern(
                "MMMM",
                locale
            )
        ),

        date.format(
            DateTimeFormatter.ofPattern(
                "MMM",
                locale
            )
        ),

        date.format(
            DateTimeFormatter.ofPattern(
                "dd MMMM yyyy",
                locale
            )
        ),

        date.format(
            DateTimeFormatter.ofPattern(
                "d MMMM yyyy",
                locale
            )
        ),

        date.format(
            DateTimeFormatter.ofPattern(
                "MMMM yyyy",
                locale
            )
        ),

        date.format(
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy",
                locale
            )
        ),

        date.format(
            DateTimeFormatter.ofPattern(
                "dd-MM-yyyy",
                locale
            )
        )
    )

    return searchableValues.any { value ->
        value.lowercase(locale).contains(query)
    }
}

sealed interface AttendanceHistorySearchIntent {

    data class QueryChanged(
        val query: String
    ) : AttendanceHistorySearchIntent

    data object QueryCleared :
        AttendanceHistorySearchIntent

    data class SessionClicked(
        val date: LocalDate
    ) : AttendanceHistorySearchIntent

    data object RetryClicked :
        AttendanceHistorySearchIntent
}

sealed interface AttendanceHistorySearchEffect {

    data class NavigateToDetails(
        val semester: String,
        val branch: String,
        val subject: String,
        val date: LocalDate
    ) : AttendanceHistorySearchEffect
}
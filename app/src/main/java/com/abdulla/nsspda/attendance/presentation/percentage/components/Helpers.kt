package com.abdulla.nsspda.attendance.presentation.percentage.components


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.abdulla.nsspda.attendance.domain.model.AttendanceRiskLevel
import com.abdulla.nsspda.attendance.presentation.percentage.AttendanceStudentSort
import java.util.Locale


@Composable
fun riskContainerColor(
    riskLevel: AttendanceRiskLevel
) = when (riskLevel) {
    AttendanceRiskLevel.EXCELLENT ->
        MaterialTheme.colorScheme.primary

    AttendanceRiskLevel.GOOD ->
        MaterialTheme.colorScheme.tertiary

    AttendanceRiskLevel.WARNING ->
        MaterialTheme.colorScheme.secondary

    AttendanceRiskLevel.CRITICAL ->
        MaterialTheme.colorScheme.error

    AttendanceRiskLevel.NO_DATA ->
        MaterialTheme.colorScheme.outline
}

fun riskLevelForPercentage(
    percentage: Double
): AttendanceRiskLevel {
    return when {
        percentage >= 90.0 ->
            AttendanceRiskLevel.EXCELLENT

        percentage >= 75.0 ->
            AttendanceRiskLevel.GOOD

        percentage >= 60.0 ->
            AttendanceRiskLevel.WARNING

        else ->
            AttendanceRiskLevel.CRITICAL
    }
}

fun AttendanceRiskLevel.displayName():
        String {
    return when (this) {
        AttendanceRiskLevel.EXCELLENT ->
            "Excellent"

        AttendanceRiskLevel.GOOD ->
            "Good"

        AttendanceRiskLevel.WARNING ->
            "Warning"

        AttendanceRiskLevel.CRITICAL ->
            "Critical"

        AttendanceRiskLevel.NO_DATA ->
            "No data"
    }
}

fun AttendanceStudentSort.displayName():
        String {
    return when (this) {
        AttendanceStudentSort.LOWEST_ATTENDANCE ->
            "Lowest attendance"

        AttendanceStudentSort.HIGHEST_ATTENDANCE ->
            "Highest attendance"

        AttendanceStudentSort.NAME ->
            "Student name"

        AttendanceStudentSort.USN ->
            "USN"
    }
}

fun Double.formatPercentage():
        String {
    return String.format(
        Locale.getDefault(),
        "%.1f%%",
        this
    )
}
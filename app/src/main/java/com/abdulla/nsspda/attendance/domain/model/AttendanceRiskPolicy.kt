package com.abdulla.nsspda.attendance.domain.model

object AttendanceRiskPolicy {

    const val EXCELLENT_MINIMUM = 90.0
    const val GOOD_MINIMUM = 75.0
    const val WARNING_MINIMUM = 60.0

    fun classify(
        percentage: Double,
        totalSessions: Int
    ): AttendanceRiskLevel {
        if (totalSessions <= 0) {
            return AttendanceRiskLevel.NO_DATA
        }

        return when {
            percentage >= EXCELLENT_MINIMUM -> {
                AttendanceRiskLevel.EXCELLENT
            }

            percentage >= GOOD_MINIMUM -> {
                AttendanceRiskLevel.GOOD
            }

            percentage >= WARNING_MINIMUM -> {
                AttendanceRiskLevel.WARNING
            }

            else -> {
                AttendanceRiskLevel.CRITICAL
            }
        }
    }
}
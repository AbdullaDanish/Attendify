package com.abdulla.nsspda.attendance.domain.model

import java.time.LocalDate
import kotlin.math.abs

enum class AttendanceRiskLevel {
    EXCELLENT,
    GOOD,
    WARNING,
    CRITICAL,
    NO_DATA
}

data class StudentAttendanceAnalytics(
    val usn: String,
    val studentName: String,
    val presentSessions: Int,
    val absentSessions: Int,
    val totalSessions: Int,
    val percentage: Double,
    val riskLevel: AttendanceRiskLevel
)

data class AttendanceTrendPoint(
    val date: LocalDate,
    val presentCount: Int,
    val absentCount: Int,
    val totalCount: Int,
    val percentage: Double
)

data class AttendanceDistribution(
    val excellentCount: Int,
    val goodCount: Int,
    val warningCount: Int,
    val criticalCount: Int,
    val noDataCount: Int
) {
    val totalStudents: Int
        get() {
            return excellentCount +
                    goodCount +
                    warningCount +
                    criticalCount +
                    noDataCount
        }
}

data class AttendanceAnalytics(
    val startDate: LocalDate,
    val endDate: LocalDate,

    val sessionCount: Int,
    val studentCount: Int,

    val classAveragePercentage: Double,
    val highestPercentage: Double?,
    val lowestPercentage: Double?,

    val totalPresentEntries: Int,
    val totalAbsentEntries: Int,

    val students: List<StudentAttendanceAnalytics>,
    val trend: List<AttendanceTrendPoint>,
    val distribution: AttendanceDistribution
) {
    val hasData: Boolean
        get() = sessionCount > 0 && students.isNotEmpty()

    val bestPerformers: List<StudentAttendanceAnalytics>
        get() {
            val highest = highestPercentage
                ?: return emptyList()

            return students.filter {
                approximatelyEqual(
                    first = it.percentage,
                    second = highest
                )
            }
        }

    val studentsNeedingAttention:
            List<StudentAttendanceAnalytics>
        get() {
            return students.filter {
                it.riskLevel ==
                        AttendanceRiskLevel.WARNING ||
                        it.riskLevel ==
                        AttendanceRiskLevel.CRITICAL
            }
        }
}

private fun approximatelyEqual(
    first: Double,
    second: Double
): Boolean {
    return abs(
        first - second
    ) < 0.001
}
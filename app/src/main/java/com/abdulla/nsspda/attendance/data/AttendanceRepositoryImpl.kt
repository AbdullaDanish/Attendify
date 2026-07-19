package com.abdulla.nsspda.attendance.data

import com.abdulla.nsspda.attendance.data.local.StudentAttendance
import com.abdulla.nsspda.attendance.data.local.StudentAttendanceDao
import com.abdulla.nsspda.attendance.data.local.toDatabaseDate
import com.abdulla.nsspda.attendance.data.local.toLocalDate
import com.abdulla.nsspda.attendance.domain.model.AttendanceAnalytics
import com.abdulla.nsspda.attendance.domain.model.AttendanceDistribution
import com.abdulla.nsspda.attendance.domain.model.AttendancePercentage
import com.abdulla.nsspda.attendance.domain.repository.AttendanceRepository
import com.abdulla.nsspda.attendance.domain.model.AttendanceRiskLevel
import com.abdulla.nsspda.attendance.domain.model.AttendanceRiskPolicy
import com.abdulla.nsspda.attendance.domain.model.AttendanceSaveResult
import com.abdulla.nsspda.attendance.domain.model.AttendanceSummary
import com.abdulla.nsspda.attendance.domain.model.AttendanceTrendPoint
import com.abdulla.nsspda.attendance.domain.model.StudentAttendanceAnalytics
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: StudentAttendanceDao,
    private val zoneId: ZoneId
) : AttendanceRepository {

    override fun observeAttendanceForDate(
        semester: String,
        branch: String,
        subject: String,
        date: Date
    ): Flow<List<StudentAttendance>> {
        return attendanceDao.observeAttendanceForDate(
            semester = semester,
            branch = branch,
            subject = subject,
            date = date
        )
    }

    override fun observeAttendanceSummaries(
        semester: String,
        branch: String,
        subject: String
    ): Flow<List<AttendanceSummary>> {
        return attendanceDao.observeAttendanceSummaries(
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    override suspend fun saveAttendance(
        attendanceList: List<StudentAttendance>
    ): AttendanceSaveResult {
        val transactionResult =
            attendanceDao.saveAttendanceForClass(
                attendanceList
            )

        return AttendanceSaveResult(
            totalStudents = attendanceList.size,
            insertedCount =
                transactionResult.insertedCount,
            updatedCount =
                transactionResult.updatedCount
        )
    }

    override suspend fun deleteAttendanceForDate(
        semester: String,
        branch: String,
        subject: String,
        date: Date
    ): Int {
        return attendanceDao.deleteAttendanceForDate(
            semester = semester,
            branch = branch,
            subject = subject,
            date = date
        )
    }

    override suspend fun calculatePercentages(
        semester: String,
        branch: String,
        subject: String,
        startDate: Date,
        endDate: Date
    ): List<AttendancePercentage> {
        require(!startDate.after(endDate)) {
            "Start date must not be after end date."
        }

        val records =
            attendanceDao.getAttendanceBetweenDates(
                semester = semester,
                branch = branch,
                subject = subject,
                startDate = startDate,
                endDate = endDate
            )

        return records
            .groupBy { attendance ->
                attendance.usn
            }
            .map { (usn, studentRecords) ->
                val totalClasses =
                    studentRecords.size

                val presentClasses =
                    studentRecords.count {
                        it.present
                    }

                val percentage =
                    if (totalClasses == 0) {
                        0.0
                    } else {
                        presentClasses.toDouble() /
                                totalClasses.toDouble() *
                                100.0
                    }

                AttendancePercentage(
                    usn = usn,
                    studentName =
                        studentRecords.first()
                            .studentName,
                    presentClasses =
                        presentClasses,
                    totalClasses =
                        totalClasses,
                    percentage = percentage
                )
            }
            .sortedBy {
                it.usn
            }
    }
    override suspend fun calculateAnalytics(
        semester: String,
        branch: String,
        subject: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): AttendanceAnalytics {
        require(semester.isNotBlank()) {
            "Semester is required."
        }

        require(branch.isNotBlank()) {
            "Branch is required."
        }

        require(subject.isNotBlank()) {
            "Subject is required."
        }

        require(!startDate.isAfter(endDate)) {
            "Start date must not be after end date."
        }

        val startDatabaseDate =
            startDate.toDatabaseDate(zoneId)

        /*
         * Include the complete selected end date.
         * Database attendance values are normalized to
         * start-of-day, so using endDate start-of-day is valid.
         */
        val endDatabaseDate =
            endDate.toDatabaseDate(zoneId)

        val records =
            attendanceDao.getAttendanceBetweenDates(
                semester = semester,
                branch = branch,
                subject = subject,
                startDate = startDatabaseDate,
                endDate = endDatabaseDate
            )

        if (records.isEmpty()) {
            return emptyAnalytics(
                startDate = startDate,
                endDate = endDate
            )
        }

        val recordsByDate =
            records.groupBy { record ->
                record.date.toLocalDate(zoneId)
            }

        val trend =
            recordsByDate
                .map { (date, sessionRecords) ->
                    val presentCount =
                        sessionRecords.count {
                            it.present
                        }

                    val totalCount =
                        sessionRecords.size

                    val absentCount =
                        totalCount - presentCount

                    AttendanceTrendPoint(
                        date = date,
                        presentCount = presentCount,
                        absentCount = absentCount,
                        totalCount = totalCount,
                        percentage = calculatePercentage(
                            presentCount = presentCount,
                            totalCount = totalCount
                        )
                    )
                }
                .sortedBy {
                    it.date
                }

        /*
         * Group by normalized USN so that differences in
         * letter case or surrounding spaces do not create
         * duplicate student analytics.
         */
        val studentAnalytics =
            records
                .groupBy { record ->
                    record.usn.normalizedUsn()
                }
                .map { (normalizedUsn, studentRecords) ->
                    val totalSessions =
                        studentRecords.size

                    val presentSessions =
                        studentRecords.count {
                            it.present
                        }

                    val absentSessions =
                        totalSessions -
                                presentSessions

                    val percentage =
                        calculatePercentage(
                            presentCount =
                                presentSessions,
                            totalCount =
                                totalSessions
                        )

                    val latestRecord =
                        studentRecords.maxByOrNull {
                            it.date.time
                        } ?: studentRecords.first()

                    StudentAttendanceAnalytics(
                        usn = normalizedUsn,
                        studentName =
                            latestRecord.studentName,
                        presentSessions =
                            presentSessions,
                        absentSessions =
                            absentSessions,
                        totalSessions =
                            totalSessions,
                        percentage =
                            percentage,
                        riskLevel =
                            AttendanceRiskPolicy.classify(
                                percentage =
                                    percentage,
                                totalSessions =
                                    totalSessions
                            )
                    )
                }
                .sortedWith(
                    compareBy<StudentAttendanceAnalytics> {
                        it.percentage
                    }.thenBy {
                        it.usn
                    }
                )

        val totalPresentEntries =
            records.count {
                it.present
            }

        val totalAbsentEntries =
            records.size -
                    totalPresentEntries

        val classAverage =
            calculatePercentage(
                presentCount = totalPresentEntries,
                totalCount = records.size
            )

        val percentages =
            studentAnalytics
                .filter {
                    it.totalSessions > 0
                }
                .map {
                    it.percentage
                }

        return AttendanceAnalytics(
            startDate = startDate,
            endDate = endDate,
            sessionCount = recordsByDate.size,
            studentCount = studentAnalytics.size,
            classAveragePercentage =
                classAverage,
            highestPercentage =
                percentages.maxOrNull(),
            lowestPercentage =
                percentages.minOrNull(),
            totalPresentEntries =
                totalPresentEntries,
            totalAbsentEntries =
                totalAbsentEntries,
            students = studentAnalytics,
            trend = trend,
            distribution =
                createDistribution(
                    students = studentAnalytics
                )
        )
    }
}

private fun emptyAnalytics(
    startDate: LocalDate,
    endDate: LocalDate
): AttendanceAnalytics {
    return AttendanceAnalytics(
        startDate = startDate,
        endDate = endDate,
        sessionCount = 0,
        studentCount = 0,
        classAveragePercentage = 0.0,
        highestPercentage = null,
        lowestPercentage = null,
        totalPresentEntries = 0,
        totalAbsentEntries = 0,
        students = emptyList(),
        trend = emptyList(),
        distribution =
            AttendanceDistribution(
                excellentCount = 0,
                goodCount = 0,
                warningCount = 0,
                criticalCount = 0,
                noDataCount = 0
            )
    )
}

private fun createDistribution(
    students: List<StudentAttendanceAnalytics>
): AttendanceDistribution {
    return AttendanceDistribution(
        excellentCount =
            students.count {
                it.riskLevel ==
                        AttendanceRiskLevel.EXCELLENT
            },
        goodCount =
            students.count {
                it.riskLevel ==
                        AttendanceRiskLevel.GOOD
            },
        warningCount =
            students.count {
                it.riskLevel ==
                        AttendanceRiskLevel.WARNING
            },
        criticalCount =
            students.count {
                it.riskLevel ==
                        AttendanceRiskLevel.CRITICAL
            },
        noDataCount =
            students.count {
                it.riskLevel ==
                        AttendanceRiskLevel.NO_DATA
            }
    )
}

private fun calculatePercentage(
    presentCount: Int,
    totalCount: Int
): Double {
    if (totalCount <= 0) {
        return 0.0
    }

    return presentCount.toDouble() /
            totalCount.toDouble() *
            100.0
}

private fun String.normalizedUsn(): String {
    return trim()
        .uppercase()
}
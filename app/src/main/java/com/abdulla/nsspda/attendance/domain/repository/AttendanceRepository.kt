package com.abdulla.nsspda.attendance.domain.repository

import com.abdulla.nsspda.attendance.data.local.StudentAttendance
import com.abdulla.nsspda.attendance.domain.model.AttendanceAnalytics
import com.abdulla.nsspda.attendance.domain.model.AttendancePercentage
import com.abdulla.nsspda.attendance.domain.model.AttendanceSaveResult
import com.abdulla.nsspda.attendance.domain.model.AttendanceSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Date

interface AttendanceRepository {

    fun observeAttendanceForDate(
        semester: String,
        branch: String,
        subject: String,
        date: Date
    ): Flow<List<StudentAttendance>>

    fun observeAttendanceSummaries(
        semester: String,
        branch: String,
        subject: String
    ): Flow<List<AttendanceSummary>>

    suspend fun saveAttendance(
        attendanceList: List<StudentAttendance>
    ): AttendanceSaveResult

    suspend fun deleteAttendanceForDate(
        semester: String,
        branch: String,
        subject: String,
        date: Date
    ): Int

    suspend fun calculatePercentages(
        semester: String,
        branch: String,
        subject: String,
        startDate: Date,
        endDate: Date
    ): List<AttendancePercentage>

    suspend fun calculateAnalytics(
        semester: String,
        branch: String,
        subject: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): AttendanceAnalytics
}
package com.abdulla.nsspda.attendance.domain.model.export

import com.abdulla.nsspda.attendance.data.local.StudentAttendance
import java.time.LocalDate

interface AttendanceExporter {

    suspend fun exportAttendance(
        attendance: List<StudentAttendance>,
        semester: String,
        branch: String,
        subject: String,
        date: LocalDate
    ): AttendanceExportResult
}
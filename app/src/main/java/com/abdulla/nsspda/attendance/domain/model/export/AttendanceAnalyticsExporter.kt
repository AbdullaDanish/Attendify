package com.abdulla.nsspda.attendance.domain.model.export

import com.abdulla.nsspda.attendance.domain.model.AttendanceAnalytics

interface AttendanceAnalyticsExporter {

    suspend fun exportAnalytics(
        analytics: AttendanceAnalytics,
        semester: String,
        branch: String,
        subject: String
    ): AttendanceAnalyticsExportResult
}
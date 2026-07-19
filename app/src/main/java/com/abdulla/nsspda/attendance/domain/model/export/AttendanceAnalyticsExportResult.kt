package com.abdulla.nsspda.attendance.domain.model.export

import android.net.Uri

data class AttendanceAnalyticsExportResult(
    val uri: Uri,
    val fileName: String,
    val mimeType: String,
    val exportedStudentCount: Int
)
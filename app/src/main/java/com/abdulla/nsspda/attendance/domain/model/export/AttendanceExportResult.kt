package com.abdulla.nsspda.attendance.domain.model.export

import android.net.Uri

data class AttendanceExportResult(
    val uri: Uri,
    val fileName: String,
    val mimeType: String,
    val exportedRowCount: Int
)
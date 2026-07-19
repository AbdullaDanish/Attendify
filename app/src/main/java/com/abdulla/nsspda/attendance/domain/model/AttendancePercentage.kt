package com.abdulla.nsspda.attendance.domain.model

data class AttendancePercentage(
    val usn: String,
    val studentName: String,
    val presentClasses: Int,
    val totalClasses: Int,
    val percentage: Double
)
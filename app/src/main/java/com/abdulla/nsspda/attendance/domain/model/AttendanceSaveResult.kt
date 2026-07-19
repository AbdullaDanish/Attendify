package com.abdulla.nsspda.attendance.domain.model

data class AttendanceSaveResult(
    val totalStudents: Int,
    val insertedCount: Int,
    val updatedCount: Int
) {
    val wasSuccessful: Boolean
        get() = totalStudents == insertedCount + updatedCount
}
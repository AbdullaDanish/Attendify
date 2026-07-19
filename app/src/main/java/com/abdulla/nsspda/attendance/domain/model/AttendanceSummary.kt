package com.abdulla.nsspda.attendance.domain.model

import androidx.room.ColumnInfo
import java.util.Date

data class AttendanceSummary(
    @ColumnInfo(name = "sem")
    val semester: String,

    @ColumnInfo(name = "branch")
    val branch: String,

    @ColumnInfo(name = "subject")
    val subject: String,

    @ColumnInfo(name = "date")
    val date: Date,

    @ColumnInfo(name = "total_count")
    val totalCount: Int,

    @ColumnInfo(name = "present_count")
    val presentCount: Int,

    @ColumnInfo(name = "absent_count")
    val absentCount: Int
) {
    val attendancePercentage: Double
        get() {
            if (totalCount <= 0) {
                return 0.0
            }

            return presentCount.toDouble() /
                    totalCount.toDouble() *
                    100.0
        }
}
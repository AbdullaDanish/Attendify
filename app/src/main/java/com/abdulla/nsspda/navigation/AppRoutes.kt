package com.abdulla.nsspda.navigation

import android.net.Uri

object AppRoutes {

    const val SEMESTER_LIST =
        "semesterListScreen"

    const val CLASS_OVERVIEW_PATTERN =
        "class_overview/{semester}/{branch}/{subject}"

    const val STUDENTS_PATTERN =
        "students/{semester}/{branch}/{subject}"

    const val ATTENDANCE_HISTORY_PATTERN =
        "attendance_history/{semester}/{branch}/{subject}"

    const val ATTENDANCE_MARKING_PATTERN =
        "attendance_marking/{semester}/{branch}/{subject}"

    const val ATTENDANCE_DETAILS_PATTERN =
        "attendance_details/{semester}/{branch}/{subject}/{date}"

    const val ATTENDANCE_PERCENTAGE_PATTERN =
        "attendance_percentage/{semester}/{branch}/{subject}"

    fun classOverview(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return "class_overview/" +
                "${semester.encode()}/" +
                "${branch.encode()}/" +
                subject.encode()
    }

    fun students(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return "students/" +
                "${semester.encode()}/" +
                "${branch.encode()}/" +
                subject.encode()
    }

    fun attendanceHistory(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return "attendance_history/" +
                "${semester.encode()}/" +
                "${branch.encode()}/" +
                subject.encode()
    }

    fun attendanceMarking(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return "attendance_marking/" +
                "${semester.encode()}/" +
                "${branch.encode()}/" +
                subject.encode()
    }

    fun attendanceDetails(
        semester: String,
        branch: String,
        subject: String,
        date: String
    ): String {
        return "attendance_details/" +
                "${semester.encode()}/" +
                "${branch.encode()}/" +
                "${subject.encode()}/" +
                date.encode()
    }

    fun attendancePercentage(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return "attendance_percentage/" +
                "${semester.encode()}/" +
                "${branch.encode()}/" +
                subject.encode()
    }

    private fun String.encode(): String {
        return Uri.encode(this)
    }
}
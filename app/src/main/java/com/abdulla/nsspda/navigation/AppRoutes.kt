package com.abdulla.nsspda.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry

object AppRoutes {

    const val ARG_SEMESTER = "semester"
    const val ARG_BRANCH = "branch"
    const val ARG_SUBJECT = "subject"
    const val ARG_DATE = "date"

    const val SEMESTER_LIST =
        "semesterListScreen"

    const val CLASS_OVERVIEW_PATTERN =
        "class_overview/{$ARG_SEMESTER}/{$ARG_BRANCH}/{$ARG_SUBJECT}"

    const val STUDENTS_PATTERN =
        "students/{$ARG_SEMESTER}/{$ARG_BRANCH}/{$ARG_SUBJECT}"

    const val ATTENDANCE_HISTORY_PATTERN =
        "attendance_history/{$ARG_SEMESTER}/{$ARG_BRANCH}/{$ARG_SUBJECT}"

    const val ATTENDANCE_HISTORY_SEARCH_PATTERN =
        "attendance_history_search/{$ARG_SEMESTER}/{$ARG_BRANCH}/{$ARG_SUBJECT}"

    const val ATTENDANCE_MARKING_PATTERN =
        "attendance_marking/{$ARG_SEMESTER}/{$ARG_BRANCH}/{$ARG_SUBJECT}"

    const val ATTENDANCE_DETAILS_PATTERN =
        "attendance_details/{$ARG_SEMESTER}/{$ARG_BRANCH}/{$ARG_SUBJECT}/{$ARG_DATE}"

    const val ATTENDANCE_PERCENTAGE_PATTERN =
        "attendance_percentage/{$ARG_SEMESTER}/{$ARG_BRANCH}/{$ARG_SUBJECT}"

    fun classOverview(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return buildClassRoute(
            baseRoute = "class_overview",
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    fun students(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return buildClassRoute(
            baseRoute = "students",
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    fun attendanceHistory(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return buildClassRoute(
            baseRoute = "attendance_history",
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    fun attendanceHistorySearch(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return buildClassRoute(
            baseRoute = "attendance_history_search",
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    fun attendanceMarking(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return buildClassRoute(
            baseRoute = "attendance_marking",
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    fun attendanceDetails(
        semester: String,
        branch: String,
        subject: String,
        date: String
    ): String {
        return buildString {
            append("attendance_details")
            append("/")
            append(semester.encode())
            append("/")
            append(branch.encode())
            append("/")
            append(subject.encode())
            append("/")
            append(date.encode())
        }
    }

    fun attendancePercentage(
        semester: String,
        branch: String,
        subject: String
    ): String {
        return buildClassRoute(
            baseRoute = "attendance_percentage",
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    fun decodeArgument(
        value: String?
    ): String {
        return Uri.decode(value.orEmpty())
    }

    private fun buildClassRoute(
        baseRoute: String,
        semester: String,
        branch: String,
        subject: String
    ): String {
        return buildString {
            append(baseRoute)
            append("/")
            append(semester.encode())
            append("/")
            append(branch.encode())
            append("/")
            append(subject.encode())
        }
    }

    private fun String.encode(): String {
        return Uri.encode(this)
    }
}

fun NavBackStackEntry.decodedArgument(
    key: String
): String {
    return AppRoutes.decodeArgument(
        arguments?.getString(key)
    )
}
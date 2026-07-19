package com.abdulla.nsspda.semester.domain

import com.abdulla.nsspda.semester.data.Semester
import kotlinx.coroutines.flow.Flow

interface SemesterRepository {

    fun observeSemesters(): Flow<List<Semester>>

    suspend fun addSemester(
        semester: Semester
    ): AddSemesterResult

    suspend fun deleteSemester(
        semester: Semester
    ): Boolean
}

sealed interface AddSemesterResult {

    data object Success : AddSemesterResult

    data object Duplicate : AddSemesterResult
}
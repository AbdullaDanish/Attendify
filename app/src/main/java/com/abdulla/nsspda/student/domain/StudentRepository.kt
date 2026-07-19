package com.abdulla.nsspda.student.domain

import com.abdulla.nsspda.student.data.local.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {

    fun observeStudents(
        semester: String,
        branch: String,
        subject: String
    ): Flow<List<Student>>

    suspend fun addStudent(
        student: Student
    ): AddStudentResult

    suspend fun importStudents(
        students: List<Student>
    ): StudentBulkImportResult

    suspend fun deleteStudent(
        student: Student
    ): Boolean

    suspend fun deleteAllStudents(
        semester: String,
        branch: String,
        subject: String
    ): Int

    suspend fun deleteStudents(
        studentIds: List<Long>
    ): Int

    fun observeStudentCount(
        semester: String,
        branch: String,
        subject: String
    ): Flow<Int>
}

sealed interface AddStudentResult {

    data object Success : AddStudentResult

    data object Duplicate : AddStudentResult
}
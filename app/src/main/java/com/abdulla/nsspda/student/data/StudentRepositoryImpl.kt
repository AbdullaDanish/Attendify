package com.abdulla.nsspda.student.data

import com.abdulla.nsspda.student.data.local.Student
import com.abdulla.nsspda.student.data.local.StudentDao
import com.abdulla.nsspda.student.domain.AddStudentResult
import com.abdulla.nsspda.student.domain.StudentBulkImportResult
import com.abdulla.nsspda.student.domain.StudentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao
) : StudentRepository {

    override fun observeStudents(
        semester: String,
        branch: String,
        subject: String
    ): Flow<List<Student>> {
        return studentDao.observeStudentsForClass(
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    override suspend fun addStudent(
        student: Student
    ): AddStudentResult {
        val insertedId =
            studentDao.insertStudent(student)

        return if (insertedId == -1L) {
            AddStudentResult.Duplicate
        } else {
            AddStudentResult.Success
        }
    }

    override suspend fun importStudents(
        students: List<Student>
    ): StudentBulkImportResult {
        if (students.isEmpty()) {
            return StudentBulkImportResult(
                requestedCount = 0,
                importedCount = 0,
                existingStudentCount = 0
            )
        }

        val insertionResults =
            studentDao.insertStudentsInTransaction(
                students
            )

        val importedCount =
            insertionResults.count { rowId ->
                rowId != -1L
            }

        val existingCount =
            insertionResults.count { rowId ->
                rowId == -1L
            }

        return StudentBulkImportResult(
            requestedCount = students.size,
            importedCount = importedCount,
            existingStudentCount = existingCount
        )
    }

    override suspend fun deleteStudent(
        student: Student
    ): Boolean {
        return studentDao.deleteStudent(student) > 0
    }

    override suspend fun deleteAllStudents(
        semester: String,
        branch: String,
        subject: String
    ): Int {
        return studentDao.deleteAllStudentsForClass(
            semester = semester,
            branch = branch,
            subject = subject
        )
    }

    override fun observeStudentCount(
        semester: String,
        branch: String,
        subject: String
    ): Flow<Int> {
        return studentDao.observeStudentCount(
            semester = semester,
            branch = branch,
            subject = subject
        )
    }
}
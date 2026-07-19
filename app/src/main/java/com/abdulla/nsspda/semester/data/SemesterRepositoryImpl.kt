package com.abdulla.nsspda.semester.data

import com.abdulla.nsspda.semester.domain.AddSemesterResult
import com.abdulla.nsspda.semester.domain.SemesterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SemesterRepositoryImpl @Inject constructor(
    private val semesterDao: SemesterDao
) : SemesterRepository {

    override fun observeSemesters(): Flow<List<Semester>> {
        return semesterDao.observeAll()
    }

    override suspend fun addSemester(
        semester: Semester
    ): AddSemesterResult {
        val insertedRowId = semesterDao.insert(semester)

        return if (insertedRowId == -1L) {
            AddSemesterResult.Duplicate
        } else {
            AddSemesterResult.Success
        }
    }

    override suspend fun deleteSemester(
        semester: Semester
    ): Boolean {
        return semesterDao.delete(semester) > 0
    }
}
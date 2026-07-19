package com.abdulla.nsspda.student.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudent(
        student: Student
    ): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudents(
        students: List<Student>
    ): List<Long>

    @Transaction
    suspend fun insertStudentsInTransaction(
        students: List<Student>
    ): List<Long> {
        if (students.isEmpty()) {
            return emptyList()
        }

        return insertStudents(students)
    }

    @Update
    suspend fun updateStudent(
        student: Student
    ): Int

    @Delete
    suspend fun deleteStudent(
        student: Student
    ): Int

    @Query(
        """
        SELECT *
        FROM student
        WHERE sem = :semester
          AND branch = :branch
          AND subject = :subject
        ORDER BY usn COLLATE NOCASE
        """
    )
    fun observeStudentsForClass(
        semester: String,
        branch: String,
        subject: String
    ): Flow<List<Student>>

    @Query(
        """
        DELETE FROM student
        WHERE sem = :semester
          AND branch = :branch
          AND subject = :subject
        """
    )
    suspend fun deleteAllStudentsForClass(
        semester: String,
        branch: String,
        subject: String
    ): Int

    @Query(
        """
    SELECT COUNT(*)
    FROM student
    WHERE sem = :semester
      AND branch = :branch
      AND subject = :subject
    """
    )
    fun observeStudentCount(
        semester: String,
        branch: String,
        subject: String
    ): Flow<Int>
}
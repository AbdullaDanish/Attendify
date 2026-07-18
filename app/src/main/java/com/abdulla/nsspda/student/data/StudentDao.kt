package com.abdulla.nsspda.student.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM student ORDER BY usn")
    fun getAllData(): LiveData<List<Student>>

    @Query("SELECT * FROM student WHERE sem = :semester AND branch = :branch ORDER BY usn")
    suspend fun getStudentsBySemesterAndBranch(semester: String, branch: String): List<Student>


    @Query("DELETE FROM student")
    suspend fun deleteAllStudents()

    @Query("DELETE FROM student WHERE sem = :semester AND branch = :branch")
    suspend fun deleteAllStudentsBySemesterAndBranch(semester: String, branch: String)
}

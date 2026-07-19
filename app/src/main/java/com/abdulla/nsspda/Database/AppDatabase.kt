package com.abdulla.nsspda.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abdulla.nsspda.Converters
import com.abdulla.nsspda.attendance.data.local.StudentAttendance
import com.abdulla.nsspda.attendance.data.local.StudentAttendanceDao
import com.abdulla.nsspda.semester.data.Semester
import com.abdulla.nsspda.semester.data.SemesterDao
import com.abdulla.nsspda.student.data.local.Student
import com.abdulla.nsspda.student.data.local.StudentDao

@Database(
    entities = [
        Semester::class,
        StudentAttendance::class,
        Student::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getSemesterDao(): SemesterDao

    abstract fun getStudentAttendanceDao(): StudentAttendanceDao

    abstract fun getStudentData(): StudentDao

    companion object {
        const val NAME = "Student_DB"
    }
}
package com.abdulla.nsspda.semester.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abdulla.nsspda.Converters
import com.abdulla.nsspda.attendance.data.StudentAttendance
import com.abdulla.nsspda.attendance.data.StudentAttendanceDao
import com.abdulla.nsspda.student.data.Student
import com.abdulla.nsspda.student.data.StudentDao

@Database(entities = [Semester::class, StudentAttendance::class,Student::class], version = 1)
@TypeConverters(Converters::class)
abstract class SemesterDatabase : RoomDatabase(){
    companion object{
        const val NAME = "Student_DB"

    }
    abstract fun getSemesterDao(): SemesterDao
    abstract fun getStudentAttendanceDao(): StudentAttendanceDao
    abstract fun getStudentData(): StudentDao
}
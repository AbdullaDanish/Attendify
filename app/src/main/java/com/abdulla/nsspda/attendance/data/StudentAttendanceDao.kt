package com.abdulla.nsspda.attendance.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.abdulla.nsspda.attendance.viewmodel.AttendanceSummary
import java.util.Date

@Dao
interface StudentAttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: StudentAttendance)

    @Query("SELECT * FROM student_attendance WHERE sem = :semester AND branch = :branch AND date = :date AND subject = :subject ORDER BY usn")
    suspend fun getAttendanceBySemesterBranchDate(semester: String, branch: String,subject: String, date: Date): List<StudentAttendance>

    @Query("""
    SELECT DISTINCT date, sem, branch, subject 
    FROM student_attendance 
    WHERE sem = :semester AND branch = :branch AND subject = :subject
    ORDER BY date
""")
    suspend fun getUniqueAttendanceSummaries(semester: String, branch: String, subject: String): List<AttendanceSummary>


    @Delete
    suspend fun deleteAttendance(attendance: StudentAttendance)

    @Query("DELETE FROM student_attendance WHERE sem = :semester AND branch = :branch AND date = :date AND subject  = :subject")
    suspend fun deleteAttendanceBySemesterBranchDate(semester: String, branch: String,subject: String, date: Date)

    @Update
    suspend fun updateAttendanceList(attendanceList: List<StudentAttendance>)


    @Query("""
    SELECT * FROM student_attendance 
    WHERE sem = :semester AND branch = :branch AND subject = :subject 
    AND date BETWEEN :startDate AND :endDate
    ORDER BY usn
""")
    suspend fun getAttendanceBetweenDates(
        semester: String,
        branch: String,
        subject: String,
        startDate: Date,
        endDate: Date
    ): List<StudentAttendance>

}

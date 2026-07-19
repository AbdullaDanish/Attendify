package com.abdulla.nsspda.attendance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.abdulla.nsspda.attendance.domain.model.AttendanceSummary
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface StudentAttendanceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttendance(
        attendance: StudentAttendance
    ): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttendanceList(
        attendance: List<StudentAttendance>
    ): List<Long>

    @Update
    suspend fun updateAttendance(
        attendance: StudentAttendance
    ): Int

    @Update
    suspend fun updateAttendanceList(
        attendanceList: List<StudentAttendance>
    ): Int

    @Delete
    suspend fun deleteAttendance(
        attendance: StudentAttendance
    ): Int

    @Query(
        """
        SELECT *
        FROM student_attendance
        WHERE sem = :semester
          AND branch = :branch
          AND subject = :subject
          AND date = :date
        ORDER BY usn COLLATE NOCASE
        """
    )
    fun observeAttendanceForDate(
        semester: String,
        branch: String,
        subject: String,
        date: Date
    ): Flow<List<StudentAttendance>>

    @Query(
        """
        SELECT *
        FROM student_attendance
        WHERE sem = :semester
          AND branch = :branch
          AND subject = :subject
          AND date = :date
        ORDER BY usn COLLATE NOCASE
        """
    )
    suspend fun getAttendanceForDate(
        semester: String,
        branch: String,
        subject: String,
        date: Date
    ): List<StudentAttendance>

    @Query(
        """
        SELECT *
        FROM student_attendance
        WHERE sem = :semester
          AND branch = :branch
          AND subject = :subject
          AND usn = :usn
          AND date = :date
        LIMIT 1
        """
    )
    suspend fun getAttendanceRecord(
        semester: String,
        branch: String,
        subject: String,
        usn: String,
        date: Date
    ): StudentAttendance?

    @Query(
        """
    SELECT
        sem,
        branch,
        subject,
        date,
        COUNT(*) AS total_count,
        SUM(
            CASE
                WHEN present = 1 THEN 1
                ELSE 0
            END
        ) AS present_count,
        SUM(
            CASE
                WHEN present = 0 THEN 1
                ELSE 0
            END
        ) AS absent_count
    FROM student_attendance
    WHERE sem = :semester
      AND branch = :branch
      AND subject = :subject
    GROUP BY
        sem,
        branch,
        subject,
        date
    ORDER BY date DESC
    """
    )
    fun observeAttendanceSummaries(
        semester: String,
        branch: String,
        subject: String
    ): Flow<List<AttendanceSummary>>

    @Query(
        """
        DELETE FROM student_attendance
        WHERE sem = :semester
          AND branch = :branch
          AND subject = :subject
          AND date = :date
        """
    )
    suspend fun deleteAttendanceForDate(
        semester: String,
        branch: String,
        subject: String,
        date: Date
    ): Int

    @Query(
        """
        SELECT *
        FROM student_attendance
        WHERE sem = :semester
          AND branch = :branch
          AND subject = :subject
          AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC, usn COLLATE NOCASE
        """
    )
    suspend fun getAttendanceBetweenDates(
        semester: String,
        branch: String,
        subject: String,
        startDate: Date,
        endDate: Date
    ): List<StudentAttendance>

    @Transaction
    suspend fun saveAttendanceForClass(
        attendanceList: List<StudentAttendance>
    ): AttendanceTransactionResult {
        if (attendanceList.isEmpty()) {
            return AttendanceTransactionResult(
                insertedCount = 0,
                updatedCount = 0
            )
        }

        var insertedCount = 0
        var updatedCount = 0

        attendanceList.forEach { attendance ->
            val insertedId =
                insertAttendance(attendance)

            if (insertedId == -1L) {
                val existing = getAttendanceRecord(
                    semester = attendance.semester,
                    branch = attendance.branch,
                    subject = attendance.subject,
                    usn = attendance.usn,
                    date = attendance.date
                )

                if (existing != null) {
                    val updatedRows = updateAttendance(
                        attendance.copy(
                            id = existing.id
                        )
                    )

                    updatedCount += updatedRows
                }
            } else {
                insertedCount++
            }
        }

        return AttendanceTransactionResult(
            insertedCount = insertedCount,
            updatedCount = updatedCount
        )
    }
}

data class AttendanceTransactionResult(
    val insertedCount: Int,
    val updatedCount: Int
)
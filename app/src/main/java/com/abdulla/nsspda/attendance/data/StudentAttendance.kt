package com.abdulla.nsspda.attendance.data
import androidx.room.*
import com.abdulla.nsspda.semester.data.Semester
import java.util.Date

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Semester::class,
            parentColumns = ["semester", "branch","subject"],
            childColumns = ["sem", "branch","subject"],
            onDelete = ForeignKey.CASCADE
        ),
//        ForeignKey(
//            entity = Student::class,
//            parentColumns = ["usn", "student_name"],
//            childColumns = ["usn", "stdname"],
//            onDelete = ForeignKey.CASCADE
//        )
    ],
    indices = [Index(value = ["sem", "branch", "subject", "usn", "date"], unique = true)],
    tableName = "student_attendance"
)
data class StudentAttendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "stdname") val studentName: String,
    @ColumnInfo(name = "usn") val usn: String,
    @ColumnInfo(name = "sem") val semester: String,
    @ColumnInfo(name = "branch") val branch: String,
    @ColumnInfo(name = "subject") val subject: String,
    @ColumnInfo(name = "present") val present: Boolean,
    @ColumnInfo(name = "date") val date: Date
)



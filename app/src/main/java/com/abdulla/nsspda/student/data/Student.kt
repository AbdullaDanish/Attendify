package com.abdulla.nsspda.student.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abdulla.nsspda.semester.data.Semester

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Semester::class,
            parentColumns = ["semester", "branch","subject"],
            childColumns = ["sem", "branch","subject"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sem", "branch"]),
        Index(value = ["usn", "student_name"], unique = true)
    ],
    tableName = "student"
)
data class Student(
    @PrimaryKey val usn: String,
    @ColumnInfo(name = "student_name") val studentName: String,
    @ColumnInfo(name = "sem") val semester: String,
    @ColumnInfo(name = "branch") val branch: String,
    @ColumnInfo(name = "subject") val subject: String
)




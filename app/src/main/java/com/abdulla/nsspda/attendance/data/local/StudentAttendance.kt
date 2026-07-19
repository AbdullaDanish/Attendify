package com.abdulla.nsspda.attendance.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abdulla.nsspda.semester.data.Semester
import java.util.Date

@Entity(
    tableName = "student_attendance",
    foreignKeys = [
        ForeignKey(
            entity = Semester::class,
            parentColumns = [
                "semester",
                "branch",
                "subject"
            ],
            childColumns = [
                "sem",
                "branch",
                "subject"
            ],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "sem",
                "branch",
                "subject",
                "usn",
                "date"
            ],
            unique = true
        ),
        Index(
            value = [
                "sem",
                "branch",
                "subject",
                "date"
            ]
        )
    ]
)
data class StudentAttendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "stdname")
    val studentName: String,

    @ColumnInfo(name = "usn")
    val usn: String,

    @ColumnInfo(name = "sem")
    val semester: String,

    @ColumnInfo(name = "branch")
    val branch: String,

    @ColumnInfo(name = "subject")
    val subject: String,

    @ColumnInfo(name = "present")
    val present: Boolean,

    @ColumnInfo(name = "date")
    val date: Date
)
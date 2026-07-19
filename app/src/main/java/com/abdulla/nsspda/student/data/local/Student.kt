package com.abdulla.nsspda.student.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abdulla.nsspda.semester.data.Semester

@Entity(
    tableName = "student",
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
            onDelete = ForeignKey.Companion.CASCADE,
            onUpdate = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [
        Index(
            name = "index_student_class",
            value = [
                "sem",
                "branch",
                "subject"
            ]
        ),
        Index(
            name = "index_student_usn_class_unique",
            value = [
                "usn",
                "sem",
                "branch",
                "subject"
            ],
            unique = true
        )
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "student_id")
    val id: Long = 0,

    @ColumnInfo(name = "usn")
    val usn: String,

    @ColumnInfo(name = "student_name")
    val studentName: String,

    @ColumnInfo(name = "sem")
    val semester: String,

    @ColumnInfo(name = "branch")
    val branch: String,

    @ColumnInfo(name = "subject")
    val subject: String
)
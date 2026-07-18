package com.abdulla.nsspda.semester.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "semester_table",
    indices = [
//        Index(value = ["semester", "branch"], unique = true),
        Index(value = ["semester", "branch", "subject"], unique = true)
    ]
)
data class Semester(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val semester: String,
    val branch: String,
    val subject: String
)

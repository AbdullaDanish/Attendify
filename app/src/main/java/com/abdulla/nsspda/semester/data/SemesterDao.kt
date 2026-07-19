package com.abdulla.nsspda.semester.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(semester: Semester): Long

    @Update
    suspend fun update(semester: Semester): Int

    @Delete
    suspend fun delete(semester: Semester): Int

    @Query(
        """
        SELECT * FROM semester_table
        ORDER BY semester COLLATE NOCASE,
                 branch COLLATE NOCASE,
                 subject COLLATE NOCASE
        """
    )
    fun observeAll(): Flow<List<Semester>>
}
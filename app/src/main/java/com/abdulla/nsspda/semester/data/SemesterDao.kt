package com.abdulla.nsspda.semester.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface SemesterDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun insert(semester: Semester)

    @Update
    suspend fun update(semester: Semester)

    @Delete
    fun delete(semester: Semester)


//    @Query("SELECT * FROM semester_table WHERE id  = :id")
//     fun getData(id: Int): LiveData<Semester?>


    @Query("SELECT * FROM semester_table ORDER BY semester")
    fun getAllData(): LiveData<List<Semester>>
}
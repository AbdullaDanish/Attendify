package com.abdulla.nsspda

import android.app.Application
import androidx.room.Room
import com.abdulla.nsspda.semester.data.SemesterDatabase

class MainApplication : Application() {

    companion object {
        lateinit var semesterDatabase: SemesterDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the Room database
        semesterDatabase = Room.databaseBuilder(
            applicationContext,
            SemesterDatabase::class.java,
            SemesterDatabase.NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}

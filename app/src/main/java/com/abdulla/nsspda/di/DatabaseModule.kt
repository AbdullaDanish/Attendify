package com.abdulla.nsspda.di

import android.content.Context
import androidx.room.Room
import com.abdulla.nsspda.attendance.data.local.StudentAttendanceDao
import com.abdulla.nsspda.semester.data.SemesterDao
import com.abdulla.nsspda.Database.AppDatabase
import com.abdulla.nsspda.student.data.local.StudentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSemesterDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.NAME
        ).build()
    }

    @Provides
    fun provideSemesterDao(
        database: AppDatabase
    ): SemesterDao {
        return database.getSemesterDao()
    }

    @Provides
    fun provideStudentAttendanceDao(
        database: AppDatabase
    ): StudentAttendanceDao {
        return database.getStudentAttendanceDao()
    }

    @Provides
    fun provideStudentDao(
        database: AppDatabase
    ): StudentDao {
        return database.getStudentData()
    }
}
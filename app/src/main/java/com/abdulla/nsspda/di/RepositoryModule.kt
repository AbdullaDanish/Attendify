package com.abdulla.nsspda.di

import com.abdulla.nsspda.attendance.data.AttendanceRepositoryImpl
import com.abdulla.nsspda.attendance.domain.repository.AttendanceRepository
import com.abdulla.nsspda.semester.data.SemesterRepositoryImpl
import com.abdulla.nsspda.semester.domain.SemesterRepository
import com.abdulla.nsspda.student.data.StudentRepositoryImpl
import com.abdulla.nsspda.student.data.imports.StudentImportRepositoryImpl
import com.abdulla.nsspda.student.domain.StudentImportRepository
import com.abdulla.nsspda.student.domain.StudentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSemesterRepository(
        implementation: SemesterRepositoryImpl
    ): SemesterRepository

    @Binds
    @Singleton
    abstract fun bindStudentRepository(
        implementation: StudentRepositoryImpl
    ): StudentRepository

    @Binds
    @Singleton
    abstract fun bindStudentImportRepository(
        implementation: StudentImportRepositoryImpl
    ): StudentImportRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        implementation: AttendanceRepositoryImpl
    ): AttendanceRepository
}
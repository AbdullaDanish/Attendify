package com.abdulla.nsspda.di

import com.abdulla.nsspda.attendance.data.export.ExcelAttendanceAnalyticsExporter
import com.abdulla.nsspda.attendance.data.export.ExcelAttendanceExporter
import com.abdulla.nsspda.attendance.domain.model.export.AttendanceAnalyticsExporter
import com.abdulla.nsspda.attendance.domain.model.export.AttendanceExporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExporterModule {

    @Binds
    @Singleton
    abstract fun bindAttendanceExporter(
        implementation: ExcelAttendanceExporter
    ): AttendanceExporter

    @Binds
    @Singleton
    abstract fun bindAttendanceAnalyticsExporter(
        implementation: ExcelAttendanceAnalyticsExporter
    ): AttendanceAnalyticsExporter
}

package com.abdulla.nsspda.attendance.data.local

import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun LocalDate.toDatabaseDate(
    zoneId: ZoneId
): Date {
    return Date.from(
        atStartOfDay(zoneId).toInstant()
    )
}

fun Date.toLocalDate(
    zoneId: ZoneId
): LocalDate {
    return toInstant()
        .atZone(zoneId)
        .toLocalDate()
}
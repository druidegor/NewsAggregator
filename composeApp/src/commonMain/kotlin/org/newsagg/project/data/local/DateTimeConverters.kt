@file:OptIn(ExperimentalTime::class)

package org.newsagg.project.data.local

import androidx.room.TypeConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class DateTimeConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }
}
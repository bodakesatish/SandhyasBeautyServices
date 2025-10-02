package com.bodakesatish.sandhyasbeautyservices.data.source.local.convertors

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@TypeConverters
class DateConverter {

    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let {
            Date(dateLong)
        }
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

}
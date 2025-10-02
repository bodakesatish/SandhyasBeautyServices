package com.bodakesatish.sandhyasbeautyservices.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateHelper {

    const val DATE_FORMAT_yyyy_MM_dd = "yyyy-MM-dd"
    const val DATE_FORMAT_dd_MMM_yyyy = "dd-MMM-yyyy"

    fun getFormattedDate(date: Date, format: String = DATE_FORMAT_yyyy_MM_dd): String {
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        return sdf.format(date)
    }

    fun getFormattedTime(date: Date, format: String = DATE_FORMAT_yyyy_MM_dd): String {
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        return sdf.format(date)
    }


    // Helper function to format time
    fun formatTime(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour < 12) "AM" else "PM"
        val hourIn12HourFormat = (hour % 12).let { if (it == 0) 12 else it }
        return String.format("%02d:%02d %s", hourIn12HourFormat, minute, amPm)
    }

    fun formatDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getDayOfWeek(currentDate: Date): String {
        val simpleDateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return simpleDateFormat.format(currentDate)
    }

    fun getCurrentMonthStartDateInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1) // First day of the current month
        return getStartOfDayInMillis(calendar.timeInMillis)
    }

    fun getCurrentMonthEndDateInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) // Last day of current month
        return getEndOfDayInMillis(calendar.timeInMillis)
    }

    // Ensure you have these or similar:
    fun getStartOfDayInMillis(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getEndOfDayInMillis(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    fun getFormattedDate(date: Date): String { // Or (dateInMillis: Long)
        // Your existing formatting logic
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }

}
package com.bodakesatish.sandhyasbeautyservices.util


import androidx.fragment.app.FragmentManager
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar
import java.util.Date

object AppTimePicker {

    fun showTimePicker(fragmentManager: FragmentManager, selectedTime: Date , titleText: String = "Select Time", onTimeSelected: (Calendar, String) -> Unit) {
        val currentTime = Calendar.getInstance().apply {
            time = selectedTime
        }
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H) // Or TimeFormat.CLOCK_24H
            .setHour(currentTime.get(Calendar.HOUR_OF_DAY))
            .setMinute(currentTime.get(Calendar.MINUTE))
            .setTitleText(titleText)
            .build()

        timePicker.addOnPositiveButtonClickListener {

            currentTime.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            currentTime.set(Calendar.MINUTE, timePicker.minute)
            onTimeSelected(currentTime, formatTime(timePicker.hour, timePicker.minute))
        }

        // Assuming you are calling this from a Fragment
        timePicker.show(fragmentManager, "time_picker")
    }

    // Helper function to format time
    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val hourIn12HourFormat = (hour % 12).let { if (it == 0) 12 else it }
        return String.format("%02d:%02d %s", hourIn12HourFormat, minute, amPm)
    }

}
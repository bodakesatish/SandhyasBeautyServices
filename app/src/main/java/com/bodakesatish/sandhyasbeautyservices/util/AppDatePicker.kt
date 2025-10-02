package com.bodakesatish.sandhyasbeautyservices.util

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar
import java.util.Date

object AppDatePicker {

    fun showDatePicker(fragmentManager: FragmentManager, selectedDate: Date, titleText: String = "Select Date", onDateSelected: (Calendar, String) -> Unit) {
        val calendar = Calendar.getInstance().apply {
            time = selectedDate
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(titleText)
            .setSelection(calendar.timeInMillis)
            .build()

        datePicker.show(fragmentManager, titleText)

        datePicker.addOnPositiveButtonClickListener { selection ->
            calendar.timeInMillis = selection
            val formattedDate =
                DateHelper.getFormattedDate(calendar.time, DateHelper.DATE_FORMAT_dd_MMM_yyyy)

            onDateSelected(calendar, formattedDate)

        }

    }
}
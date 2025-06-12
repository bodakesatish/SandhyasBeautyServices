package com.bodakesatish.sandhyasbeautyservices.compose.appointment

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppointmentSummaryViewModel @Inject constructor() : ViewModel() {
    // Add ViewModel logic here if needed
    val message = "Welcome to Appointment Summary Screen!"
}
package com.bodakesatish.sandhyasbeautyservices.compose.appointment

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodakesatish.sandhyasbeautyservices.compose.GenericScreen

@Composable
fun AppointmentSummaryScreen(viewModel: AppointmentSummaryViewModel = hiltViewModel()) {
    GenericScreen(title = "Appointment Summary Screen", message = viewModel.message)
}
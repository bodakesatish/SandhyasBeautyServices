package com.bodakesatish.sandhyasbeautyservices.compose.appointment

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodakesatish.sandhyasbeautyservices.compose.GenericScreen

@Composable
fun AppointmentEditScreen(viewModel: AppointmentEditViewModel = hiltViewModel()) {
    GenericScreen(title = "New/Edit Appointment Screen", message = viewModel.message)
}
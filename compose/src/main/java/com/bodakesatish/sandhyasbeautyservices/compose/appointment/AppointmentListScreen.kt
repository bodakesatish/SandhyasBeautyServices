package com.bodakesatish.sandhyasbeautyservices.compose.appointment

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodakesatish.sandhyasbeautyservices.compose.GenericScreen

@Composable
fun AppointmentListScreen(viewModel: AppointmentListViewModel = hiltViewModel()) {
    GenericScreen(title = "Appointment List Screen", message = viewModel.message)
}
package com.bodakesatish.sandhyasbeautyservices.compose.customer

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodakesatish.sandhyasbeautyservices.compose.GenericScreen

@Composable
fun EditCustomerScreen(viewModel: EditCustomerViewModel = hiltViewModel()) {
    GenericScreen(title = "New/Edit Customer Screen", message = viewModel.message)
}
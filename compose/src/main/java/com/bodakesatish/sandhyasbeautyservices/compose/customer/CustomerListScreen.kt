package com.bodakesatish.sandhyasbeautyservices.compose.customer

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodakesatish.sandhyasbeautyservices.compose.GenericScreen

@Composable
fun CustomerListScreen(viewModel: CustomerListViewModel = hiltViewModel()) {
    GenericScreen(title = "Customer List Screen", message = viewModel.message)
}
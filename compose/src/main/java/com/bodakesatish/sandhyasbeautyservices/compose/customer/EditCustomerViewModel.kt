package com.bodakesatish.sandhyasbeautyservices.compose.customer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditCustomerViewModel @Inject constructor() : ViewModel() {
    // Add ViewModel logic here if needed
    val message = "Welcome to New/Edit Customer Screen!"
}
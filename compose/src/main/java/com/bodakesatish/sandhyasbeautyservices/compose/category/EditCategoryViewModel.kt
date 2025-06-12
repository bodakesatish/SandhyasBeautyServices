package com.bodakesatish.sandhyasbeautyservices.compose.category

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditCategoryViewModel @Inject constructor() : ViewModel() {
    // Add ViewModel logic here if needed
    val message = "Welcome to New/Edit Category Screen!"
}
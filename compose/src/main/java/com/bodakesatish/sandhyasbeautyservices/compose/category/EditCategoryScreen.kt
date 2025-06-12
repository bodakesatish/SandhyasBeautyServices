package com.bodakesatish.sandhyasbeautyservices.compose.category

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodakesatish.sandhyasbeautyservices.compose.GenericScreen

@Composable
fun EditCategoryScreen(viewModel: EditCategoryViewModel = hiltViewModel()) {
    GenericScreen(title = "New/Edit Category Screen", message = viewModel.message)
}
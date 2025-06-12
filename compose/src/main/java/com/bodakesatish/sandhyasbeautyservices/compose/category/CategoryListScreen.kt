package com.bodakesatish.sandhyasbeautyservices.compose.category

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodakesatish.sandhyasbeautyservices.compose.GenericScreen

@Composable
fun CategoryListScreen(viewModel: CategoryListViewModel = hiltViewModel()) {
    GenericScreen(title = "Category List Screen", message = viewModel.message)
}
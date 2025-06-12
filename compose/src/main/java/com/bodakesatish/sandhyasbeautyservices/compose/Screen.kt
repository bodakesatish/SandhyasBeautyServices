package com.bodakesatish.sandhyasbeautyservices.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route:String, val title:String, val icon: ImageVector) {
    object AppointmentList : Screen("appointment_list", "Dashboard", Icons.Filled.Home)
    object CustomerList : Screen("customer_list", "Customer", Icons.Filled.Person)
    object CategoryList : Screen("category_list", "Services", Icons.Filled.Settings)
}

val bottomNavItems = listOf(
    Screen.AppointmentList,
    Screen.CustomerList,
    Screen.CategoryList,
)
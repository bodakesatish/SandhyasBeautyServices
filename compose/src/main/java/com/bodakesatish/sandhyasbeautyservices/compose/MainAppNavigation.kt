package com.bodakesatish.sandhyasbeautyservices.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bodakesatish.sandhyasbeautyservices.compose.appointment.AppointmentListScreen
import com.bodakesatish.sandhyasbeautyservices.compose.category.CategoryListScreen
import com.bodakesatish.sandhyasbeautyservices.compose.customer.CustomerListScreen
import com.bodakesatish.sandhyasbeautyservices.compose.ui.theme.SandhyasBeautyServicesTheme

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        //label = { screen.title },
                        label = { Text(screen.title) }, // Keep the label
                        selected = currentDestination?.hierarchy?.any{ it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {//startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.AppointmentList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.AppointmentList.route) { AppointmentListScreen(hiltViewModel()) }
            composable(Screen.CustomerList.route) { CustomerListScreen(hiltViewModel()) }
            composable(Screen.CategoryList.route) { CategoryListScreen(hiltViewModel()) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultAppPreview() {
    SandhyasBeautyServicesTheme {
        MainAppNavigation()
    }
}
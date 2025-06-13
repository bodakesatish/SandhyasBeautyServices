package com.bodakesatish.sandhyasbeautyservices.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bodakesatish.sandhyasbeautyservices.compose.category.EditCategoryScreen
import com.bodakesatish.sandhyasbeautyservices.compose.category.ListCategoryScreen
import com.bodakesatish.sandhyasbeautyservices.compose.ui.theme.SandhyasBeautyServicesTheme

// Placeholder for screens not yet implemented
@Composable
fun PlaceholderScreen(screenTitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Screen: $screenTitle")
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentDestination = navBackStackEntry?.destination


    // Determine if the bottom bar should be shown
    val shouldShowBottomBar = when (currentDestination?.route) {
        AppDestinations.CategoryFeature.ADD_CATEGORY_ROUTE,
        AppDestinations.CategoryFeature.EDIT_CATEGORY_ROUTE_PATTERN -> false // Hide for these routes
        else -> bottomNavItems.any { it.route == currentDestination?.route } // Show if it's a bottom nav item
    }


    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                MainBottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun MainBottomNavigationBar(
    navController: NavHostController,
    currentDestination: NavDestination? // Pass currentDestination
    ) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = stringResource(screen.titleResId)
                    )
                },
                label = { Text(stringResource(screen.titleResId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
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

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.MainScreens.CATEGORIES, // Default start screen
        modifier = modifier
    ) {
        // --- Bottom Navigation Screens ---
        composable(AppDestinations.MainScreens.CATEGORIES) {
            ListCategoryScreen(
                // ViewModels are typically injected via hiltViewModel() within the screen itself
                onNavigateToAddCategory = {
                    navController.navigate(AppDestinations.CategoryFeature.ADD_CATEGORY_ROUTE)
                },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate(
                        AppDestinations.CategoryFeature.editCategoryRoute(
                            categoryId
                        )
                    )
                }
            )
        }
        composable(AppDestinations.MainScreens.APPOINTMENTS) {
            // Replace with your actual AppointmentListScreen when ready
            PlaceholderScreen(stringResource(R.string.bottom_nav_appointments))
        }
        composable(AppDestinations.MainScreens.CUSTOMERS) {
            // Replace with your actual CustomerListScreen when ready
            PlaceholderScreen(stringResource(R.string.bottom_nav_customers))
        }

        // --- Category Feature Screens ---
        composable(AppDestinations.CategoryFeature.ADD_CATEGORY_ROUTE) {
            EditCategoryScreen(
                // ViewModel is injected via hiltViewModel()
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = AppDestinations.CategoryFeature.EDIT_CATEGORY_ROUTE_PATTERN,
            arguments = AppDestinations.CategoryFeature.editCategoryArguments
        ) {
            // EditCategoryViewModel will pick up 'categoryId' from SavedStateHandle via Hilt
            EditCategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Other Feature Screens can be added here ---
    }
}


@Preview(showBackground = true)
@Composable
fun MainAppNavigationPreview() {
    SandhyasBeautyServicesTheme { // Use your app's theme
        MainAppNavigation()
    }
}
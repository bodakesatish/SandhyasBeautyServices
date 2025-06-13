package com.bodakesatish.sandhyasbeautyservices.compose

//package com.bodakesatish.sandhyasbeautyservices.navigation // Or your app's navigation package

import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Defines all navigation routes and arguments in the application.
 */
object AppDestinations {

    // --- Bottom Navigation Screens ---
    object MainScreens { // Grouping main bottom nav screens for clarity
        const val CATEGORIES = "categories_list_route"
        const val APPOINTMENTS = "appointments_list_route"
        const val CUSTOMERS = "customers_list_route"
    }

    // --- Category Feature Specific Routes ---
    object CategoryFeature {
        const val CATEGORY_ID_ARG = "categoryId"
        const val EDIT_CATEGORY_ROUTE_PATTERN = "category_edit_route/{$CATEGORY_ID_ARG}" // Pattern for NavHost
        const val ADD_CATEGORY_ROUTE = "category_add_route" // Distinct route for adding

        fun editCategoryRoute(categoryId: Int): String = "category_edit_route/$categoryId"

        val editCategoryArguments: List<NamedNavArgument> = listOf(
            navArgument(CATEGORY_ID_ARG) {
                type = NavType.IntType
                // No defaultValue for required argument in edit flow
            }
        )
        // No arguments needed for addCategoryRoute if it starts fresh
    }

    // Add other feature routes similarly (e.g., AppointmentFeature, CustomerFeature)
}

/**
 * Represents screens for bottom navigation.
 * Note: Consider moving this to a more UI-centric package if preferred.
 */
sealed class BottomNavItem(val route: String, val titleResId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Categories : BottomNavItem(
        route = AppDestinations.MainScreens.CATEGORIES,
        titleResId = R.string.bottom_nav_categories, // Replace with your R.string
        icon = androidx.compose.material.icons.Icons.Filled.List
    )
    object Appointments : BottomNavItem(
        route = AppDestinations.MainScreens.APPOINTMENTS,
        titleResId = R.string.bottom_nav_appointments,
        icon = androidx.compose.material.icons.Icons.Filled.DateRange
    )
    object Customers : BottomNavItem(
        route = AppDestinations.MainScreens.CUSTOMERS,
        titleResId = R.string.bottom_nav_customers,
        icon = androidx.compose.material.icons.Icons.Filled.Person
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Categories,
    BottomNavItem.Appointments,
    BottomNavItem.Customers,
)

//Changes in AppDestinations.kt:•Grouped routes by feature (MainScreens, CategoryFeature).•Clearer naming for route patterns and argument names.•Distinguished ADD_CATEGORY_ROUTE from EDIT_CATEGORY_ROUTE_PATTERN for better clarity in NavHost and ViewModel logic.•Renamed Screen sealed class to BottomNavItem for more specific meaning. Used actual R.string references (you'll need to create these strings).
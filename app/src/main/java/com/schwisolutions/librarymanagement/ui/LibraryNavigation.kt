package com.schwisolutions.librarymanagement.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlinx Serialization.
 *
 * Official docs reference:
 * - developer.android.com/guide/navigation/navigation-with-compose
 * - developer.android.com/guide/navigation/design/type-safety
 */
@Serializable
object PinRoute

@Serializable
object MainRoute

@Serializable
data class DetailsRoute(val bookId: Int)

/**
 * Development toggle: Set to true during active coding to skip entering
 * the PIN on every single run. Revert to false before submitting or demoing.
 */
private const val DEV_BYPASS_PIN = false

/**
 * Main navigation host managing app destinations.
 *
 * Route flow:
 * 1. PinRoute: Start destination (unless DEV_BYPASS_PIN is true). On PIN success,
 *    navigates to MainRoute and pops PinRoute so back exits the app.
 * 2. MainRoute: Book list dashboard. Clicking a book navigates to DetailsRoute(bookId).
 * 3. DetailsRoute: Shows metadata and cover for the selected bookId. Back button pops to MainRoute.
 */
@Composable
fun LibraryNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (DEV_BYPASS_PIN) MainRoute else PinRoute,
        modifier = modifier
    ) {
        composable<PinRoute> {
            PinScreen(
                onLoginSuccess = {
                    navController.navigate(MainRoute) {
                        popUpTo(PinRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<MainRoute> {
            MainScreen(onViewBook = { bookId ->
                navController.navigate(DetailsRoute(bookId = bookId))
            })
        }

        composable<DetailsRoute> { backStackEntry ->
            val routeData: DetailsRoute = backStackEntry.toRoute()
            DetailsScreen(
                bookId = routeData.bookId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
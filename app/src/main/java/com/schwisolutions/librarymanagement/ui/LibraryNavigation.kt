package com.schwisolutions.librarymanagement.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
object PinRoute

@Serializable
object MainRoute

@Serializable
data class DetailsRoute(val bookId: Int)


@Composable
fun LibraryNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PinRoute,
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
            DetailsScreen(bookId = routeData.bookId, onBackClick = {
                navController.popBackStack()
            })
        }

    }
}
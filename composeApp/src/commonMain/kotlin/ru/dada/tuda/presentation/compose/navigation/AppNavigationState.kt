package ru.dada.tuda.presentation.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

/**
 * Класс для управления навигацией в приложении
 */
class AppNavigationState(
    val navController: NavHostController
) {

    fun navigateToMain() {
        navController.navigate(Screen.MAIN.route) {
            popUpTo(Screen.MAIN.route) { inclusive = true }
        }
    }

    fun navigateToAuth() {
        navController.navigate(Screen.AUTH.route)
    }

    fun navigateToProfile() {
        navController.navigate(Screen.PROFILE.route)
    }

    fun navigateToEvents() {
        navController.navigate(Screen.EVENTS.route)
    }

    fun navigateToFeedback() {
        navController.navigate(Screen.FEEDBACK.route)
    }

    fun navigateToShortlist() {
        navController.navigate(Screen.SHORTLIST.route)
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    fun navigateUp() {
        navController.navigateUp()
    }
}

@Composable
fun rememberAppNavigationState(
    navController: NavHostController = rememberNavController()
): AppNavigationState {
    return remember(navController) {
        AppNavigationState(navController)
    }
}

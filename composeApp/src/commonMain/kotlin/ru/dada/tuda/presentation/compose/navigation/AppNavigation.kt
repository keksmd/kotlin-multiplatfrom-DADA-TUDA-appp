package ru.dada.tuda.presentation.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.dada.tuda.presentation.compose.screens.main.MainScreen
import ru.dada.tuda.presentation.compose.screens.auth.AuthScreen
import ru.dada.tuda.presentation.compose.screens.registration.RegistrationScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.AUTH.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.AUTH.route) {
            AuthScreen(navController::navigateToRegistration, navController::navigateToMainAndClearStack)
        }

        composable(Screen.REGISTRATION.route) {
            RegistrationScreen(navController::navigateToAuth, navController::navigateToMainAndClearStack)
        }

        composable(Screen.MAIN.route) {
            MainScreen()
        }
    }
}

/**
 * Навигация на главный экран с очисткой back stack
 * Используется после успешной авторизации/регистрации
 */
fun NavHostController.navigateToMainAndClearStack() {
    navigate(Screen.MAIN.route) {
        // Очищаем весь back stack до стартового экрана
        popUpTo(Screen.AUTH.route) {
            inclusive = true
        }
        // Предотвращаем дублирование главного экрана
        launchSingleTop = true
    }
}

/**
 * Навигация между экранами авторизации/регистрации
 */
fun NavHostController.navigateToRegistration() {
    navigate(Screen.REGISTRATION.route)
}

fun NavHostController.navigateToAuth() {
    navigate(Screen.AUTH.route) {
        popUpTo(Screen.AUTH.route) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

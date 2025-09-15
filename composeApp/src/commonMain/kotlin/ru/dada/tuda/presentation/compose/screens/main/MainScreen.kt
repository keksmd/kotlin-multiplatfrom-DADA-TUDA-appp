package ru.dada.tuda.presentation.compose.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dadatuda.composeapp.generated.resources.Res
import dadatuda.composeapp.generated.resources.ic_navigation_like_screen_active
import dadatuda.composeapp.generated.resources.ic_navigation_like_screen_inactive
import dadatuda.composeapp.generated.resources.ic_navigation_main_screen_active
import dadatuda.composeapp.generated.resources.ic_navigation_main_screen_inactive
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ru.dada.tuda.presentation.compose.navigation.MainNavigation
import ru.dada.tuda.presentation.compose.navigation.Screen

data class BottomNavItem(
    val route: String,
    val inactiveIcon: DrawableResource,
    val activeIcon: DrawableResource,
    val label: String
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(
            Screen.EVENTS.route,
            Res.drawable.ic_navigation_main_screen_inactive,
            Res.drawable.ic_navigation_main_screen_active,
            "События"
        ),
        BottomNavItem(
            Screen.SHORTLIST.route,
            Res.drawable.ic_navigation_like_screen_inactive,
            Res.drawable.ic_navigation_like_screen_active,
            "Избранное"
        )
    )
    val backgroundColor by animateColorAsState(
        if (currentDestination?.hierarchy?.any { it.route == bottomNavItems[1].route } == true) Color.White else Color.Black
    )

    // Единый радиус скруглений для верхних углов карточки с нижней навигацией
    val navBarTopRadius = 20.dp

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            Card(
                shape = RoundedCornerShape(navBarTopRadius, navBarTopRadius, 0.dp, 0.dp),
                colors = CardDefaults.cardColors(Color(0xFFF8F8F8)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.shadow(
                    8.dp,
                    RoundedCornerShape(navBarTopRadius, navBarTopRadius, 0.dp, 0.dp),
                    false
                )
            ) {
                NavigationBar(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFF8F8F8),
                            RoundedCornerShape(navBarTopRadius, navBarTopRadius, 0.dp, 0.dp)
                        ),
                    containerColor = Color(0xFFF8F8F8),
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                if (item.inactiveIcon != null) {
                                    // Используем Crossfade для плавного перехода между иконками
                                    Crossfade(currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        label = item.label) {
                                        Image(
                                            painter = painterResource(
                                                if (it)
                                                    item.activeIcon
                                                else
                                                    item.inactiveIcon
                                            ),
                                            contentDescription = item.label,
                                        )
                                    }
                                } else {
                                    Image(
                                        painter = painterResource(item.activeIcon),
                                        contentDescription = item.label,
                                    )
                                }
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Возвращаемся к начальному экрану навигации
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Избегаем нескольких копий одного экрана
                                    launchSingleTop = true
                                    // Восстанавливаем состояние при повторном выборе
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val adjustedBottomPadding =
            (paddingValues.calculateBottomPadding() - navBarTopRadius).coerceAtLeast(0.dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                // Уменьшаем нижний отступ контента на высоту скругления, чтобы NavigationBar перекрывал контент
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    bottom = adjustedBottomPadding
                )
        ) {
            MainNavigation(navController = navController, bottomOverlapPadding = navBarTopRadius)
        }
    }
}

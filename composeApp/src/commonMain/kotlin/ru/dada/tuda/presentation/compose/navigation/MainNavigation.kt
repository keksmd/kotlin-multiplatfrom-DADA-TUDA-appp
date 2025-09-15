package ru.dada.tuda.presentation.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.compose.koinInject
import ru.dada.tuda.domain.http.models.CardItem
import ru.dada.tuda.domain.http.models.singleevent.SingleEventViewModel
import ru.dada.tuda.domain.repository.ShortlistRepository
import ru.dada.tuda.presentation.compose.EventsScreen
import ru.dada.tuda.presentation.compose.screens.SingleEventScreen
import ru.dada.tuda.presentation.compose.screens.filters.EventsFiltersScreen
import ru.dada.tuda.presentation.compose.screens.filters.FiltersScreen
import ru.dada.tuda.presentation.compose.screens.shortlist.ShortListScreen

@Composable
fun MainNavigation(
    navController: NavHostController,
    startDestination: String = Screen.EVENTS.route,
    modifier: Modifier = Modifier,
    // Паддинг для компенсации перекрытия снизу на высоту скруглений
    bottomOverlapPadding: Dp = 0.dp
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.EVENTS.route) {
            // Для экрана событий фон на весь экран, а контент внутри с отступом снизу
            EventsScreen(
                navigateToFilters = { navController.navigate(Screen.FILTERS_EVENTS.route) },
                bottomContentPadding = bottomOverlapPadding
            )
        }

        composable(Screen.SHORTLIST.route) {
            // Для остальных экранов добавляем внешний отступ снизу
            ShortListScreen(
                navigateToEvent = { navController.navigate("${Screen.SINGLE_EVENT.route}/$it") },
                navigateToEvents = { navController.navigate(Screen.EVENTS.route) },
                navigateToFilters = { navController.navigate(Screen.FILTERS.route) },
                bottomOverlapPadding = bottomOverlapPadding
            )
        }

        composable(Screen.FILTERS.route) {
            FiltersScreen(
                onBack = { navController.popBackStack() },
                onApply = { navController.popBackStack() },
                bottomOverlapPadding = bottomOverlapPadding
            )
        }

        composable(Screen.FILTERS_EVENTS.route) {
            EventsFiltersScreen(
                onBack = { navController.popBackStack() },
                onApply = { navController.popBackStack() },
                bottomOverlapPadding = bottomOverlapPadding
            )
        }

        composable(
            route = "${Screen.SINGLE_EVENT.route}/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.savedStateHandle.get<String>("eventId") ?: return@composable
            val viewModel: SingleEventViewModel = koinInject()
            val shortlistRepository: ShortlistRepository = koinInject()

            // Получаем CardItem из предыдущего экрана, если он был передан
            val cardItem = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<CardItem>("cardItem")
                
            // Проверяем, пришли ли мы с экрана шортлиста
            val isFromShortlist = navController.previousBackStackEntry?.destination?.route == Screen.SHORTLIST.route

            LaunchedEffect(cardItem, isFromShortlist) {
                if (cardItem != null) {
                    // Если есть данные CardItem, загружаем их сразу
                    viewModel.loadEventFromCardItem(cardItem)
                } else {
                    // Иначе загружаем по ID
                    viewModel.loadEvent(eventId)
                }

                // Если пришли с экрана шортлиста, устанавливаем контекст
                if (isFromShortlist) {
                    val shortlistEvents = shortlistRepository.getCurrentShortlistItems()
                    viewModel.setShortlistContext(shortlistEvents, eventId)
                }
            }

            SingleEventScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                bottomContentPadding = bottomOverlapPadding
            )
        }
    }
}

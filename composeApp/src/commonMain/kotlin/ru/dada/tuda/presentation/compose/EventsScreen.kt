package ru.dada.tuda.presentation.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dadatuda.composeapp.generated.resources.Res
import dadatuda.composeapp.generated.resources.ic_filters
import dadatuda.composeapp.generated.resources.revert_icon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import ru.dada.tuda.domain.http.models.CardItem
import ru.dada.tuda.domain.http.models.event.MainViewModel
import ru.dada.tuda.domain.http.models.feedback.FeedbackViewModel
import ru.dada.tuda.domain.util.PlatformContext
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.platform.openUrl
import ru.dada.tuda.presentation.theme.BodyLargeText
import ru.dada.tuda.presentation.theme.BodyMediumText
import ru.dada.tuda.presentation.theme.TitleLargeText
import ru.dada.tuda.presentation.theme.TitleMediumText
import ru.dada.tuda.presentation.theme.colorAccent
import ru.dada.tuda.presentation.ui.components.SwipeDirection
import ru.dada.tuda.presentation.ui.components.SwipeableCardStack
import ru.dada.tuda.presentation.ui.components.SwipeableCardStackController
import ru.dada.tuda.presentation.ui.components.rememberSwipeableCardStackController

@Composable
fun EventsScreen(
    mainViewModel: MainViewModel = koinInject(),
    feedbackViewModel: FeedbackViewModel = koinInject(),
    navigateToFilters: () -> Unit = {},
    // Внутренний нижний отступ контента карточки, чтобы кнопки/текст не закрывались NavigationBar
    bottomContentPadding: Dp = 0.dp
) {
    val platformContext: PlatformContext = koinInject()
    val cardsState by mainViewModel.cardsLiveData.collectAsState()

    // Создаем контроллер на уровне экрана, чтобы он не пересоздавался
    val controller = rememberSwipeableCardStackController()

    // Создаем состояние для отслеживания завершения стопки
    var isStackFinished by remember { mutableStateOf(false) }

    // Отслеживаем изменения в контроллере и обновляем состояние
    LaunchedEffect(controller.currentIndex, controller.totalItems) {
        isStackFinished = controller.isStackFinished
    }

    Box {
        Column(
            modifier = Modifier.fillMaxSize(),
            Arrangement.spacedBy(16.dp),
            Alignment.End
        ) {

            when (val resource = cardsState) {
                is Resource.Success -> {
                    println("Cards loaded successfully: ${resource.data.size} items")
                    println("Is stack finished: $isStackFinished ${controller.currentIndex} ${controller.totalItems}")
                    val cardList = resource.data
                    if (cardList.isNotEmpty() && !isStackFinished) {
                        LazyCardStackContainer(
                            cards = cardList,
                            controller = controller,
                            openLink = { link ->
                                openUrl(link, platformContext)
                            },
                            onCardLike = { card ->
                                feedbackViewModel.handleCardSwiped(card, SwipeDirection.RIGHT)
                            },
                            onCardDislike = { card ->
                                feedbackViewModel.handleCardSwiped(card, SwipeDirection.LEFT)
                            },
                            navigateToFilters = navigateToFilters,
                            bottomOverlapPadding = bottomContentPadding
                        )
                    } else {
                        EmptyCardsState(navigateToFilters = navigateToFilters)
                    }
                }

                is Resource.Empty -> {
                    EmptyCardsState(navigateToFilters = navigateToFilters)
                }

                is Resource.Error -> {
                    ErrorCardsState(resource.message)
                }

                is Resource.Loading -> {
                    LoadingCardsState()
                }
            }
        }
    }
}

@Composable
fun LazyCardStackContainer(
    cards: List<CardItem>,
    controller: SwipeableCardStackController,
    openLink: (String?) -> Unit,
    onCardLike: (CardItem) -> Unit,
    onCardDislike: (CardItem) -> Unit,
    navigateToFilters: () -> Unit = {},
    bottomOverlapPadding: Dp = 0.dp
) {
    // Добавляем состояние для отслеживания развернутых карточек
    var expandedCardIds by remember { mutableStateOf(setOf<String?>()) }
    // Добавляем состояние для отметки, что карточка уже была раскрыта (аналог moreOpen)
    var openedCardIds by remember { mutableStateOf(setOf<String?>()) }
    // Локальные оверрайды starred по id
    var starredOverrides by remember { mutableStateOf(mapOf<String, Boolean>()) }

    SwipeableCardStack(
        cards,
        { card, direction ->
            val updatedCard = card.copy(
                moreOpen = openedCardIds.contains(card.id),
                starred = starredOverrides[card.id] ?: card.starred
            )
            when (direction) {
                SwipeDirection.LEFT -> onCardDislike(updatedCard)
                SwipeDirection.RIGHT -> onCardLike(updatedCard)
                else -> {}
            }
        },
        {},
        Modifier.fillMaxSize(),
        controller,
        emptyContent = { EmptyCardsState() }
    ) {
        val isExpanded = expandedCardIds.contains(it.id)
        val isStarred = starredOverrides[it.id] ?: it.starred
        val cardForFeedback = it.copy(
            moreOpen = openedCardIds.contains(it.id),
            starred = isStarred
        )
        SwipeableCard(
            card = it,
            isExpanded = isExpanded,
            isStarred = isStarred,
            onExpandToggle = {
                expandedCardIds = if (isExpanded) {
                    expandedCardIds - it.id
                } else {
                    if (!openedCardIds.contains(it.id)) {
                        openedCardIds = openedCardIds + it.id
                    }
                    expandedCardIds + it.id
                }
            },
            onStarredToggle = { id, newValue ->
                starredOverrides = starredOverrides + (id to newValue)
            },
            openLink = openLink,
            onLike = { onCardLike(cardForFeedback); controller.swipeRight() },
            onDislike = { onCardDislike(cardForFeedback); controller.swipeLeft() },
            returnAction = { controller.undo() },
            navigateToFilters = navigateToFilters,
            bottomOverlapPadding = bottomOverlapPadding
        )
    }
}

@Composable
fun EmptyCardsState(navigateToFilters: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_filters),
            contentDescription = "Фильтры",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp)
                .clip(CircleShape)
                .clickable(onClick = navigateToFilters)
                .padding(4.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎉 Все карточки просмотрены!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Возвращайтесь завтра за новыми мероприятиями",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { /* Обновить карточки */ },
                colors = ButtonDefaults.buttonColors(colorAccent)
            ) {
                Text("Обновить", color = Color.Black)
            }
        }
    }
}

@Composable
fun ErrorCardsState(message: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "❌ Ошибка загрузки",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message ?: "Неизвестная ошибк��",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { /* Повторить загрузку */ },
                colors = ButtonDefaults.buttonColors(colorAccent)
            ) {
                Text("Повторить", color = Color.White)
            }
        }
    }
}

@Composable
fun LoadingCardsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = colorAccent)
            Text(
                text = "Загружаем карточки...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SwipeableCard(
    card: CardItem,
    isExpanded: Boolean,
    isStarred: Boolean,
    onExpandToggle: () -> Unit,
    onStarredToggle: (String, Boolean) -> Unit,
    openLink: (String?) -> Unit,
    returnAction: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    navigateToFilters: () -> Unit = {},
    bottomOverlapPadding: Dp
) {
    // Локальное состояние для звезды, инициализируем из параметра
    var localStarred by remember(card.id, isStarred) { mutableStateOf(isStarred) }

    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RectangleShape,
    ) {
        Box(Modifier.fillMaxSize()) {
            CardContent(
                card = card,
                isExpanded = isExpanded,
                isStarred = localStarred,
                onMoreInfoToggle = onExpandToggle,
                openLink = openLink,
                returnAction = returnAction,
                onSwipeManual = { direction ->
                    when (direction) {
                        SwipeDirection.LEFT -> onDislike()
                        SwipeDirection.RIGHT -> onLike()
                        else -> {}
                    }
                },
                onStarredChange = { starred ->
                    localStarred = starred
                    onStarredToggle(card.id, starred)
                },
                navigateToFilters = navigateToFilters,
                bottomOverlapPadding = bottomOverlapPadding
            )
        }
    }
}

@Composable
fun CardContent(
    card: CardItem,
    isExpanded: Boolean,
    onMoreInfoToggle: () -> Unit,
    returnAction: () -> Unit,
    openLink: (String?) -> Unit,
    onSwipeManual: (SwipeDirection) -> Unit,
    isStarred: Boolean,
    onStarredChange: (Boolean) -> Unit,
    navigateToFilters: () -> Unit = {},
    bottomOverlapPadding: Dp
) {
    val pagerState = rememberPagerState(pageCount = { card.imageURL.size })
    var timerProgress by remember(pagerState.currentPage) { mutableFloatStateOf(0f) }

    Box(Modifier.fillMaxSize()) {
        CardBackground(
            imageUrl = card.imageURL,
            pagerState = pagerState,
            isExpanded = isExpanded,
            onProgressUpdate = { progress -> timerProgress = progress }
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = bottomOverlapPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardProgressIndicator(pagerState, timerProgress)

            AnimatedVisibility(!isExpanded, Modifier.align(Alignment.End)) {
                Icon(
                    painterResource(Res.drawable.ic_filters), null, tint = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = navigateToFilters)
                        .padding(4.dp)
                )
            }

            CardMainContent(
                card = card,
                isExpanded = isExpanded,
                openLink = openLink,
                modifier = Modifier.weight(1f)
            )

            CardActionButtons(
                isExpanded = isExpanded,
                isStarred = isStarred,
                onMoreInfoToggle = onMoreInfoToggle,
                returnAction = returnAction,
                onSwipeManual = onSwipeManual,
                onStarredChange = onStarredChange
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardBackground(
    imageUrl: List<String>,
    pagerState: PagerState,
    isExpanded: Boolean,
    onProgressUpdate: (Float) -> Unit
) {
    if (imageUrl.isEmpty()) return

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change: PointerInputChange, dragAmount: Float ->
                    println("change: $change, dragAmount: $dragAmount")
                }
            }) {
        val coroutineScope = rememberCoroutineScope()
        var autoScrollKey by remember { mutableIntStateOf(0) }

        // Автопрокрутка каждые 10 секунд с отслеживанием прогресса
        LaunchedEffect(imageUrl.size, autoScrollKey) {
            if (imageUrl.size > 1) {
                // Сначала сбрасываем прогресс до 0
                onProgressUpdate(0f)

                val timerDuration = 10000L // 10 секунд
                val updateInterval = 50L // Обновляем каждые 50мс
                val totalSteps = timerDuration / updateInterval

                for (step in 1..totalSteps) { // Начинаем с 1, чтобы избежать дублирования 0f
                    val progress = step.toFloat() / totalSteps
                    onProgressUpdate(progress)
                    delay(updateInterval)
                }

                // Переходим к следующей странице
                val nextPage = (pagerState.currentPage + 1)
                if (nextPage >= imageUrl.lastIndex) return@LaunchedEffect

                pagerState.animateScrollToPage(nextPage)
                autoScrollKey++ // Перезапускаем таймер
            }
        }

        // HorizontalPager для слайдера фотографий с отключенными жестами
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false // Отключаем ручную прокрутку жестами
        ) { page ->
            AsyncImage(
                model = imageUrl[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        }

        // Невидимые области для кликов - только если есть несколько изображений
        if (imageUrl.size > 1) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Левая треть экрана - предыдущая картинка
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            coroutineScope.launch {
                                if (pagerState.currentPage == 0) return@launch

                                val prevPage = if (pagerState.currentPage == 0) {
                                    imageUrl.size - 1
                                } else {
                                    pagerState.currentPage - 1
                                }
                                pagerState.animateScrollToPage(prevPage)
                                onProgressUpdate(0f) // Сбрасываем прогресс
                                autoScrollKey++ // Сбрасываем таймер автопрокрутки
                            }
                        }
                )

                // Средняя треть - не кликабельная область
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )

                // Правая треть экрана - следующая картинка
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            coroutineScope.launch {
                                if (pagerState.currentPage >= imageUrl.lastIndex) return@launch

                                val nextPage = (pagerState.currentPage + 1) % imageUrl.size
                                pagerState.animateScrollToPage(nextPage)
                                onProgressUpdate(0f) // Сбрасываем прогресс
                                autoScrollKey++ // Сбрасываем таймер автопрокрутки
                            }
                        }
                )
            }
        }

        // Индикаторы страниц (точки) показываем только если есть несколько изображений
        if (imageUrl.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(imageUrl.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    Color.White
                                else
                                    Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }

    // Анимированные цвета для плавного перехода
    val topColor by animateColorAsState(
        targetValue = if (isExpanded) Color.Black.copy(0.75f) else Color.Black.copy(0.1f),
        animationSpec = tween(durationMillis = 300),
        label = "topColorAnimation"
    )

    val bottomColor by animateColorAsState(
        targetValue = if (isExpanded) Color.Black.copy(0.75f) else Color.Black.copy(0.9f),
        animationSpec = tween(durationMillis = 300),
        label = "bottomColorAnimation"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to topColor,
                    1.0f to bottomColor
                )
            )
    )
}

@Composable
fun CardProgressIndicator(pagerState: PagerState, timerProgress: Float) {
    val imageCount by remember(pagerState) { mutableIntStateOf(pagerState.pageCount) }

    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)) {
        repeat(imageCount) { index ->
            val progress = when {
                index < pagerState.currentPage -> 1f
                index == pagerState.currentPage -> timerProgress
                else -> 0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .weight(1f),
                color = Color.White,
                strokeCap = StrokeCap.Round,
                trackColor = Color.White.copy(.5f),
                gapSize = 0.dp,
            )
        }
    }
}

@Composable
fun CardMainContent(
    card: CardItem,
    isExpanded: Boolean,
    openLink: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
            Icon(
                painterResource(Res.drawable.revert_icon),
                null,
                Modifier.size(24.dp),
                colorAccent
            )
            TitleLargeText(
                card.title ?: "Нет названия",
                color = Color.White
            )
        }

        Spacer(Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(Color.White.copy(.23f))) {
            Row(Modifier.padding(6.dp), Arrangement.spacedBy(6.dp)) {
                Icon(getIconOnType(card.type), null, tint = Color.White)
                Text(card.categories?.firstOrNull() ?: "Нет категории", color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        TitleMediumText(
            card.address ?: "Нет адресса",
            color = Color.White
        )

        Spacer(Modifier.height(12.dp))

        BodyMediumText(
            card.getMainScreenDate(),
            color = Color.White
        )

        Spacer(Modifier.height(4.dp))

        BodyMediumText(
            card.price?.let { "От $it₽" } ?: "Нет цены",
            color = colorAccent
        )

        Spacer(Modifier.height(16.dp))

        AnimatedContent(
            isExpanded,
            label = "CardContentAnimation"
        ) {
            if (it) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        BodyLargeText(
                            card.description ?: "Нет описания",
                            color = Color.White,
                        )
                    }

                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            Arrangement.spacedBy(16.dp),
                            Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(Res.drawable.revert_icon),
                                null,
                                Modifier.size(24.dp),
                                colorAccent
                            )
                            Button(
                                { openLink(card.referralLink) },
                                colors = ButtonDefaults.buttonColors(colorAccent)
                            ) {
                                TitleMediumText(
                                    "Перейти на сайт мероприятия",
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            } else
                BodyLargeText(
                    card.shortDescription ?: "Нет краткого описания",
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
        }
    }
}

@Composable
fun CardActionButtons(
    isExpanded: Boolean,
    isStarred: Boolean,
    onMoreInfoToggle: () -> Unit,
    returnAction: () -> Unit,
    onSwipeManual: (SwipeDirection) -> Unit,
    onStarredChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        IconButton(
            { onSwipeManual(SwipeDirection.LEFT) },
            colors = IconButtonDefaults.iconButtonColors(contentColor = colorAccent)
        ) {
            Icon(
                Icons.Default.Close,
                null,
                Modifier
                    .wrapContentHeight()
                    .aspectRatio(1f)
            )
        }

        IconButton(
            returnAction,
            colors = IconButtonDefaults.iconButtonColors(contentColor = colorAccent)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                null,
                Modifier
                    .padding(4.dp)
                    .wrapContentHeight()
                    .aspectRatio(1f)
            )
        }

        Button(
            onMoreInfoToggle,
            colors = ButtonDefaults.buttonColors(colorAccent),
        ) {
            AnimatedContent(isExpanded) {
                TitleMediumText(
                    if (!it) "Подробнее" else "Скрыть",
                    color = Color.Black
                )
            }
        }

        IconButton(
            { onStarredChange(!isStarred) },
            colors = IconButtonDefaults.iconButtonColors(contentColor = colorAccent),
        ) {
            Crossfade(isStarred) {
                Icon(
                    if (it) Icons.Default.Star else Icons.Default.StarOutline,
                    null,
                    Modifier
                        .padding(4.dp)
                        .wrapContentHeight()
                        .aspectRatio(1f)
                )
            }
        }

        IconButton(
            { onSwipeManual(SwipeDirection.RIGHT) },
            colors = IconButtonDefaults.iconButtonColors(contentColor = colorAccent),
        ) {
            Icon(
                Icons.Default.Favorite,
                null,
                Modifier
                    .wrapContentHeight()
                    .aspectRatio(1f),
            )
        }
    }
}

fun getShortCountLikes(likes: Int): String {
    return when {
        likes >= 1_000_000 -> {
            val millions = likes / 1_000_000.0
            formatNumberWithSuffix(millions, "М")
        }

        likes >= 1_000 -> {
            val thousands = likes / 1_000.0
            formatNumberWithSuffix(thousands, "К")
        }

        else -> likes.toString()
    }
}

fun formatNumberWithSuffix(value: Double, suffix: String): String {
    // Проверка на целое число
    val isInteger = value % 1 == 0.0

    // Форматируем число
    val formatted = if (isInteger) {
        value.toInt().toString()
    } else {
        // Округляем до 1 знака после запятой
        val roundedValue = (value * 10).toInt() / 10.0

        // Преобразуем в строку
        if ((roundedValue * 10).toInt() % 10 == 0) {
            // Если после запятой 0, отображаем как целое
            roundedValue.toInt().toString()
        } else {
            roundedValue.toString()
        }
    }

    // Добавляем суффикс
    return formatted + suffix
}

fun getIconOnType(type: String?): ImageVector {
    return when (type?.lowercase()) {
        "event" -> Icons.Default.Event
        "food" -> Icons.Default.Restaurant
        "entertainment" -> Icons.Default.MovieFilter
        "sport" -> Icons.Default.SportsFootball
        "culture" -> Icons.Default.Museum
        "education" -> Icons.Default.School
        "business" -> Icons.Default.Business
        "health" -> Icons.Default.LocalHospital
        "travel" -> Icons.Default.Flight
        "shopping" -> Icons.Default.ShoppingCart
        "music" -> Icons.Default.MusicNote
        "art" -> Icons.Default.Palette
        "technology" -> Icons.Default.Computer
        "nature" -> Icons.Default.Nature
        else -> Icons.Default.Category
    }
}

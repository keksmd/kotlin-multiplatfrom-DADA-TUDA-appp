package ru.dada.tuda.presentation.compose.screens

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dadatuda.composeapp.generated.resources.Res
import dadatuda.composeapp.generated.resources.ic_arrow_back_long
import dadatuda.composeapp.generated.resources.ic_arrow_next_long
import dadatuda.composeapp.generated.resources.ic_like
import dadatuda.composeapp.generated.resources.revert_icon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import ru.dada.tuda.domain.http.models.CardItem
import ru.dada.tuda.domain.http.models.singleevent.SingleEventViewModel
import ru.dada.tuda.domain.util.PlatformContext
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.presentation.compose.getIconOnType
import ru.dada.tuda.presentation.compose.screens.shortlist.CardLikesCounter
import ru.dada.tuda.presentation.compose.screens.shortlist.ShortlistDeleteDialog
import ru.dada.tuda.presentation.theme.BodyLargeText
import ru.dada.tuda.presentation.theme.BodyMediumText
import ru.dada.tuda.presentation.theme.TitleLargeText
import ru.dada.tuda.presentation.theme.TitleMediumText
import ru.dada.tuda.presentation.theme.colorAccent
import ru.dada.tuda.platform.openUrl
import ru.dada.tuda.platform.shareText

@Composable
fun SingleEventScreen(
    viewModel: SingleEventViewModel,
    onBackClick: () -> Unit,
    bottomContentPadding: Dp = 0.dp
) {
    val platformContext: PlatformContext = koinInject()
    val eventResource by viewModel.eventLiveData.collectAsState()
    val isStarred by viewModel.isFavorite.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val canNavigatePrevious by viewModel.canNavigatePrevious.collectAsState()
    val canNavigateNext by viewModel.canNavigateNext.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val resource = eventResource) {
            is Resource.Success -> {
                val cardItem = resource.data

                SingleEventCard(
                    card = cardItem,
                    isStarred = isStarred,
                    isLiked = isLiked,
                    canNavigatePrevious = canNavigatePrevious,
                    canNavigateNext = canNavigateNext,
                    onStarredToggle = { viewModel.toggleFavorite() },
                    onLikeToggle = { viewModel.toggleLike() },
                    onNavigatePrevious = { viewModel.navigateToPrevious() },
                    onNavigateNext = { viewModel.navigateToNext() },
                    onBackClick = onBackClick,
                    openLink = { link ->
                        openUrl(link, platformContext)
                    },
                    onShare = {
                        val shareTextValue = "${cardItem.title}\n${cardItem.shortDescription}\n${cardItem.referralLink}"
                        shareText(shareTextValue, platformContext)
                    },
                    bottomOverlapPadding = bottomContentPadding
                )
            }

            is Resource.Loading -> {
                SingleEventLoadingState(
                    onBackClick = onBackClick,
                    bottomContentPadding = bottomContentPadding
                )
            }

            is Resource.Error -> {
                SingleEventErrorState(
                    errorMessage = resource.message,
                    onBackClick = onBackClick,
                    onRetry = { viewModel.refreshEvent() },
                    bottomContentPadding = bottomContentPadding
                )
            }

            is Resource.Empty -> {
                SingleEventEmptyState(
                    onBackClick = onBackClick,
                    bottomContentPadding = bottomContentPadding
                )
            }

            null -> {
                SingleEventLoadingState(
                    onBackClick = onBackClick,
                    bottomContentPadding = bottomContentPadding
                )
            }
        }

        // Диалог удаления события
        if (showDeleteDialog) {
            ShortlistDeleteDialog(
                confirmAction = { viewModel.deleteEvent() },
                setDeleteDialogState = { viewModel.hideDeleteDialog() }
            )
        }
    }
}

@Composable
fun SingleEventLoadingState(
    onBackClick: () -> Unit,
    bottomContentPadding: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Black.copy(0.3f),
                    1.0f to Color.Black.copy(0.8f)
                )
            )
            .padding(16.dp)
            .padding(bottom = bottomContentPadding)
    ) {
        // Кнопка назад
        IconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.3f),
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
            )
        }

        // Индикатор загрузки
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = colorAccent,
                modifier = Modifier.size(48.dp)
            )
            TitleMediumText(
                text = "Загрузка события...",
                color = Color.White
            )
        }
    }
}

@Composable
fun SingleEventErrorState(
    errorMessage: String,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    bottomContentPadding: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Black.copy(0.3f),
                    1.0f to Color.Black.copy(0.8f)
                )
            )
            .padding(16.dp)
            .padding(bottom = bottomContentPadding)
    ) {
        // Кнопка назад
        IconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.3f),
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
            )
        }

        // Сообщение об ошибке
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painterResource(Res.drawable.revert_icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Red
            )

            TitleMediumText(
                text = "Ошибка загрузки",
                color = Color.White
            )

            BodyMediumText(
                text = errorMessage,
                color = Color.White.copy(0.7f)
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(colorAccent)
            ) {
                TitleMediumText(
                    text = "Попробовать снова",
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun SingleEventEmptyState(
    onBackClick: () -> Unit,
    bottomContentPadding: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Black.copy(0.3f),
                    1.0f to Color.Black.copy(0.8f)
                )
            )
            .padding(16.dp)
            .padding(bottom = bottomContentPadding)
    ) {
        // Кнопка назад
        IconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.3f),
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
            )
        }

        // Сообщение о пустом результате
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painterResource(Res.drawable.revert_icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White.copy(0.5f)
            )

            TitleMediumText(
                text = "Событие не найдено",
                color = Color.White
            )

            BodyMediumText(
                text = "Запрашиваемое событие не существует или было удалено",
                color = Color.White.copy(0.7f)
            )
        }
    }
}

@Composable
fun SingleEventCard(
    card: CardItem,
    isStarred: Boolean,
    isLiked: Boolean,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onStarredToggle: (Boolean) -> Unit,
    onLikeToggle: () -> Unit,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    onBackClick: () -> Unit,
    openLink: (String?) -> Unit,
    onShare: () -> Unit,
    bottomOverlapPadding: Dp
) {
    // Используем remember с key для сброса состояния при смене события
    val pagerState = rememberPagerState(pageCount = { card.imageURL.size })
    var timerProgress by remember(card.id) { mutableFloatStateOf(0f) } // Сбрасываем при смене ID

    // Сбрасываем состояние pager'а при смене события
    LaunchedEffect(card.id) {
        pagerState.scrollToPage(0)
        timerProgress = 0f
    }

    // Анимированный переход между событиями
    Crossfade(
        targetState = card.id,
        animationSpec = tween(durationMillis = 300),
        modifier = Modifier.fillMaxSize(),
        label = "EventTransition"
    ) { eventId ->
        Box(Modifier.fillMaxSize()) {
            SingleEventBackground(
                imageUrl = card.imageURL,
                pagerState = pagerState,
                onProgressUpdate = { progress -> timerProgress = progress }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = bottomOverlapPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Фиксированный индикатор прогресса
                SingleEventProgressIndicator(pagerState, timerProgress)

                // Фиксированная верхняя панель
                SingleEventTopBar(
                    onBackClick = onBackClick,
                    isStarred = isStarred,
                    onStarredToggle = onStarredToggle,
                    onShare = onShare
                )

                // Прокручиваемый контент со сбросом скролла
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SingleEventMainContent(
                            card = card,
                            openLink = openLink
                        )
                    }
                }

                // Фиксированные кнопки действий внизу
                SingleEventActionButtons(
                    isStarred = isStarred,
                    isLiked = isLiked,
                    likes = card.likes,
                    canNavigatePrevious = canNavigatePrevious,
                    canNavigateNext = canNavigateNext,
                    onStarredToggle = onStarredToggle,
                    onLikeToggle = onLikeToggle,
                    onNavigatePrevious = onNavigatePrevious,
                    onNavigateNext = onNavigateNext,
                    openLink = { openLink(card.referralLink) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleEventBackground(
    imageUrl: List<String>,
    pagerState: PagerState,
    onProgressUpdate: (Float) -> Unit
) {
    if (imageUrl.isEmpty()) return

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change: PointerInputChange, dragAmount: Float ->
                    // Можно добавить обработку жестов если нужно
                }
            }) {
        val coroutineScope = rememberCoroutineScope()
        var autoScrollKey by remember { mutableIntStateOf(0) }

        // Автопрокрутка каждые 10 секунд с отслеживанием прогресса
        LaunchedEffect(imageUrl.size, autoScrollKey) {
            if (imageUrl.size > 1) {
                onProgressUpdate(0f)

                val timerDuration = 10000L
                val updateInterval = 50L
                val totalSteps = timerDuration / updateInterval

                for (step in 1..totalSteps) {
                    val progress = step.toFloat() / totalSteps
                    onProgressUpdate(progress)
                    delay(updateInterval)
                }

                val nextPage = (pagerState.currentPage + 1)
                if (nextPage >= imageUrl.lastIndex) return@LaunchedEffect

                pagerState.animateScrollToPage(nextPage)
                autoScrollKey++
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            AsyncImage(
                model = imageUrl[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        }

        // Невидимые области для кликов
        if (imageUrl.size > 1) {
            Row(modifier = Modifier.fillMaxSize()) {
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
                                val prevPage = pagerState.currentPage - 1
                                pagerState.animateScrollToPage(prevPage)
                                onProgressUpdate(0f)
                                autoScrollKey++
                            }
                        }
                )

                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f))

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
                                onProgressUpdate(0f)
                                autoScrollKey++
                            }
                        }
                )
            }
        }

        // Индикаторы страниц (точки)
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

    Box(Modifier
        .fillMaxSize()
        .background(Color.Black.copy(0.75f)))
}

@Composable
fun SingleEventProgressIndicator(pagerState: PagerState, timerProgress: Float) {
    val imageCount by remember(pagerState) { mutableIntStateOf(pagerState.pageCount) }

    if (imageCount <= 1) return

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
                trackColor = Color.White.copy(.5f),
                gapSize = 0.dp,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SingleEventTopBar(
    onBackClick: () -> Unit,
    isStarred: Boolean,
    onStarredToggle: (Boolean) -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.3f),
                contentColor = Color.White
            )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Назад"
            )
        }
    }
}

@Composable
fun SingleEventMainContent(
    card: CardItem,
    openLink: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
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

        Card(colors = CardDefaults.cardColors(Color.White.copy(.23f))) {
            Row(Modifier.padding(6.dp), Arrangement.spacedBy(6.dp)) {
                Icon(getIconOnType(card.type), null, tint = Color.White)
                Text(card.categories?.firstOrNull() ?: "Нет категории", color = Color.White)
            }
        }

        TitleMediumText(
            card.address ?: "Нет адреса",
            color = Color.White
        )

        BodyMediumText(
            card.getMainScreenDate(),
            color = Color.White
        )

        BodyMediumText(
            card.price?.let { "От $it₽" } ?: "Нет цены",
            color = colorAccent
        )

        BodyLargeText(
            card.description ?: "Нет описания",
            color = Color.White,
        )

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

@Composable
fun SingleEventActionButtons(
    isStarred: Boolean,
    isLiked: Boolean,
    likes: Int,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onStarredToggle: (Boolean) -> Unit,
    onLikeToggle: () -> Unit,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    openLink: () -> Unit
) {
    // Ряд с 4 иконками навигации и действий
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка "назад" (предыдущее событие)
        IconButton(
            onNavigatePrevious,
            enabled = canNavigatePrevious,
            colors = IconButtonDefaults.iconButtonColors(contentColor = colorAccent),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painterResource(Res.drawable.ic_arrow_back_long),
                null,
                Modifier
                    .padding(4.dp)
                    .wrapContentHeight()
                    .aspectRatio(1f)
            )
        }

        Spacer(Modifier.weight(.5f))

        // Кнопка избранного
        IconButton(
            onClick = { onStarredToggle(!isStarred) },
            colors = IconButtonDefaults.iconButtonColors(contentColor = colorAccent),
            modifier = Modifier.weight(1f)
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

        Box(Modifier.weight(1f)) {
            Icon(
                painterResource(Res.drawable.ic_like),
                contentDescription = "Remove from favorites",
                Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .clickable { onLikeToggle() }
                    .padding(8.dp),
                tint = colorAccent
            )
            CardLikesCounter(
                likes,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
                    .padding(bottom = 8.dp)
                    .padding(start = 16.dp)
            )
        }

        Spacer(Modifier.weight(.5f))

        // Кнопка "вперед" (следующее событие)
        IconButton(
            onClick = onNavigateNext,
            enabled = canNavigateNext,
            colors = IconButtonDefaults.iconButtonColors(contentColor = colorAccent),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painterResource(Res.drawable.ic_arrow_next_long),
                null,
                Modifier
                    .padding(4.dp)
                    .wrapContentHeight()
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
fun SingleEventDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            TitleMediumText(
                "Удалить событие",
                color = Color.Black
            )
        },
        text = {
            BodyMediumText(
                "Вы уверены, что хотите удалить это событие из шортлиста?",
                color = Color.Black
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        },
        containerColor = Color.White,
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
    )
}

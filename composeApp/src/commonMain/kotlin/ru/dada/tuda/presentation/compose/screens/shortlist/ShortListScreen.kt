package ru.dada.tuda.presentation.compose.screens.shortlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dadatuda.composeapp.generated.resources.Res
import dadatuda.composeapp.generated.resources.ic_filled_star
import dadatuda.composeapp.generated.resources.ic_like
import dadatuda.composeapp.generated.resources.ic_outlined_star
import dadatuda.composeapp.generated.resources.ic_revert_icon_thin
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import ru.dada.tuda.domain.http.models.CardItem
import ru.dada.tuda.domain.http.models.shortlist.ShortlistViewModel
import ru.dada.tuda.domain.repository.PaginationState
import ru.dada.tuda.domain.util.Resource
import ru.dada.tuda.domain.util.toFormattedTwoLinesDateTime
import ru.dada.tuda.presentation.compose.getShortCountLikes
import ru.dada.tuda.presentation.theme.AutoSizeHeadlineText
import ru.dada.tuda.presentation.theme.BodyLargeText
import ru.dada.tuda.presentation.theme.BodyMediumText
import ru.dada.tuda.presentation.theme.CygreFontFamily
import coil3.size.Size as CoilSize

class SnakeCornerShape(private val radius: Dp = 20.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            val r = with(density) { radius.toPx() }

            moveTo(r, 0f)
            // Линия до начала выемки
            lineTo(w - 3 * r, 0f)
            // Плавная вогнутая кривая для выемки
            quadraticTo(w - 2 * r, 0f, w - 2 * r, r)
            // Правая и нижняя стороны
            quadraticTo(w - 2 * r, 2 * r, w - r, 2 * r)
            quadraticTo(w, 2 * r, w, 3 * r)
            lineTo(w, h - r)
            quadraticTo(w, h, w - r, h)
            lineTo(r, h)
            // Левая сторона и углы
            quadraticTo(0f, h, 0f, h - r)
            lineTo(0f, r)
            quadraticTo(0f, 0f, r, 0f)
            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SnakeCornerShape) return false
        return radius == other.radius
    }

    override fun hashCode(): Int {
        return radius.hashCode()
    }
}

@Composable
fun ShortListScreen(
    shortlistViewModel: ShortlistViewModel = koinInject(),
    navigateToEvent: (String) -> Unit,
    navigateToEvents: () -> Unit,
    navigateToFilters: () -> Unit,
    bottomOverlapPadding: Dp = 0.dp
) {
    val shortlistState by shortlistViewModel.shortlistLiveData.collectAsState()
    val paginationState by shortlistViewModel.paginationState.collectAsState()
    val isStarred by shortlistViewModel.isStarredFlow.collectAsState()
    val searchState by shortlistViewModel.searchFlow.collectAsState()
    val favoriteOverrides by shortlistViewModel.favoriteOverrides.collectAsState()
    val hasActiveFilters by shortlistViewModel.hasActiveFiltersFlow.collectAsState()

    LaunchedEffect(Unit) {
        shortlistViewModel.getShortlist()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(bottom = bottomOverlapPadding)
    ) {
        // Заголовок - всегда виден
        ShortlistHeader(
            isStarred = isStarred,
            updateStarred = shortlistViewModel::updateStarred,
            showFilters = shortlistState is Resource.Success,
            onFiltersClick = navigateToFilters,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        )

        // Строка поиска - всегда видна, но может быть неактивной
        OutlinedTextField(
            value = searchState,
            onValueChange = shortlistViewModel::updateSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(50),
//            enabled = shortlistState is Resource.Success && (shortlistState?.data?.content?.isNotEmpty() == true || searchState.isNotEmpty()), // Активна только при успешной загрузке
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    null,
                    tint = if (shortlistState is Resource.Success)
                        Color(0xFF8F8E94)
                    else
                        Color(0xFF8F8E94).copy(alpha = 0.5f)
                )
            },
            placeholder = {
                BodyLargeText(
                    "Поиск",
                    color = if (shortlistState is Resource.Success)
                        Color(0xFF8F8E94)
                    else
                        Color(0xFF8F8E94).copy(alpha = 0.5f)
                )
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color(0xFFEFEFEF),
                focusedIndicatorColor = Color(0xFFEFEFEF),
                unfocusedContainerColor = Color(0xFFEFEFEF),
                focusedContainerColor = Color(0xFFEFEFEF),
                disabledIndicatorColor = Color(0xFFEFEFEF).copy(alpha = 0.5f),
                disabledContainerColor = Color(0xFFEFEFEF).copy(alpha = 0.5f)
            )
        )

        // Контент в зависимости от состояния
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            when (val resource = shortlistState) {
                is Resource.Success -> {
                    val cardItems = resource.data.content.orEmpty().map { CardItem(it) }
                    if (cardItems.isNotEmpty()) {
                        ShortlistContent(
                            cardItems = cardItems,
                            paginationState = paginationState,
                            onLoadNextPage = shortlistViewModel::loadNextPage,
                            onCardClick = { cardItem ->
                                navigateToEvent(cardItem.id)
                            },
                            onToggleFavorite = { id, current ->
                                shortlistViewModel.toggleFavorite(id, current)
                            }
                        )
                    } else {
                        // Проверяем, не идет ли загрузка - если да, показываем загрузку вместо пустого состояния
                        if (paginationState?.isLoading == true) {
                            LoadingShortlistContent()
                        } else {
                            // Показываем разные сообщения в зависимости от наличия фильтров
                            if (hasActiveFilters) {
                                EmptyFilteredShortlistContent(
                                    onClearFilters = { shortlistViewModel.clearAllFilters() },
                                    onExploreEvents = navigateToEvents
                                )
                            } else {
                                EmptyShortlistContent(
                                    onExploreEvents = navigateToEvents
                                )
                            }
                        }
                    }
                }

                is Resource.Empty -> {
                    // Проверяем, не идет ли загрузка - если да, показываем загрузку вместо пустого состояния
                    if (paginationState?.isLoading == true) {
                        LoadingShortlistContent()
                    } else {
                        if (hasActiveFilters) {
                            EmptyFilteredShortlistContent(
                                onClearFilters = { shortlistViewModel.clearAllFilters() },
                                onExploreEvents = navigateToEvents
                            )
                        } else {
                            EmptyShortlistContent(
                                onExploreEvents = navigateToEvents
                            )
                        }
                    }
                }

                is Resource.Loading, null -> {
                    LoadingShortlistContent()
                }

                is Resource.Error -> {
                    ErrorShortlistContent(
                        message = resource.message,
                        onRetry = { shortlistViewModel.getShortlist() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun ShortlistContent(
    cardItems: List<CardItem>,
    paginationState: PaginationState? = null,
    onLoadNextPage: () -> Unit = {},
    onCardClick: (CardItem) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
) {
    Column(
        Modifier.fillMaxSize(),
        Arrangement.spacedBy(16.dp)
    ) {
        val listState =
            androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState()

        // Отслеживаем достижение конца списка
        LaunchedEffect(
            listState,
            paginationState?.hasMorePages,
            paginationState?.isLoadingMore,
            paginationState?.isLoading
        ) {
            snapshotFlow {
                val info = listState.layoutInfo
                val firstVisible = info.visibleItemsInfo.minOfOrNull { it.index } ?: 0
                val lastVisible = info.visibleItemsInfo.maxOfOrNull { it.index } ?: 0
                Triple(firstVisible, lastVisible, info.totalItemsCount)
            }
                .debounce(100)
                .distinctUntilChanged()
                .collect { (firstVisible, lastVisible, total) ->
                    if (total > 0 && firstVisible > 0 && lastVisible >= total - 4) {
                        if (paginationState?.hasMorePages == true && !paginationState.isLoadingMore && !paginationState.isLoading) {
                            onLoadNextPage()
                        }
                    }
                }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            items(
                items = cardItems,
                key = { it.id },
                contentType = { "card" }
            ) { cardItem ->
                ShortlistEventCard(
                    cardItem = cardItem,
                    onClick = { onCardClick(cardItem) },
                    onToggleFavorite = onToggleFavorite
                )
            }

            if (paginationState?.isLoadingMore == true) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    LoadingMoreIndicator()
                }
            }
        }

        // Один раз после первой загрузки проскроллим список к началу
//        var didResetScroll by rememberSaveable { mutableStateOf(false) }
//        LaunchedEffect(cardItems.isNotEmpty(), didResetScroll) {
//            if (cardItems.isNotEmpty() && !didResetScroll) {
//                listState.scrollToItem(0)
//                didResetScroll = true
//            }
//        }
//
//         Отслеживаем достижение конца списка вне item-композабла
//        LaunchedEffect(listState, paginationState?.hasMorePages, paginationState?.isLoadingMore, paginationState?.isLoading) {
//            snapshotFlow {
//                val info = listState.layoutInfo
//                val firstVisible = info.visibleItemsInfo.minOfOrNull { it.index } ?: 0
//                val lastVisible = info.visibleItemsInfo.maxOfOrNull { it.index } ?: 0
//                Triple(firstVisible, lastVisible, info.totalItemsCount)
//            }
//                .debounce(100)
//                .distinctUntilChanged()
//                .collect { (firstVisible, lastVisible, total) ->
//                    if (total > 0 && firstVisible > 0 && lastVisible >= total - 4) {
//                        if (paginationState?.hasMorePages == true && !paginationState.isLoadingMore && !paginationState.isLoading) {
//                            onLoadNextPage()
//                        }
//                    }
//                }
//        }

        // Prefetch ближайших изображений при прокрутке вниз
        // ...existing code...
    }
}

@Composable
fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
            Text(
                text = "Загрузка...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortlistEventCard(
    cardItem: CardItem,
    onClick: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalPlatformContext.current
    var isFavorite by remember(cardItem.id) { mutableStateOf(cardItem.isFavorite) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val shape = remember { SnakeCornerShape() }
    val firstImageUrl = remember(cardItem.imageURL) { cardItem.imageURL.firstOrNull() }
    val dateFormatted = remember(cardItem.date) { cardItem.date.toFormattedTwoLinesDateTime() }

    // Вычисляем целевой размер изображения в пикселях под текущую ширину карточки
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val targetSizePx = remember(windowInfo, density) {
        with(density) {
            val screenWidthDp = windowInfo.containerSize.width.dp
            val horizontalPadding = 16.dp * 2 // padding контейнера
            val spacing = 8.dp // межколоночный отступ
            val cardWidthDp = (screenWidthDp - horizontalPadding - spacing) / 2f
            val w = cardWidthDp.roundToPx() / 3
            val h = (w / 1.5f).toInt() / 3 // aspectRatio 1.5
            w to h
        }
    }
    // Лёгкий placeholder/error/fallback, чтобы избежать мерцаний и перерасчётов
    val placeholderPainter = remember { ColorPainter(Color(0xFFEFEFEF)) }

    Box(modifier = modifier) {
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "StarAnimation",
            modifier = Modifier.align(Alignment.TopEnd)
        ) { targetFavorite ->
            Image(
                painterResource(if (targetFavorite) Res.drawable.ic_filled_star else Res.drawable.ic_outlined_star),
                contentDescription = if (targetFavorite) "Remove from favorites" else "Add to favorites",
                Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .clickable {
                        val prev = isFavorite
                        isFavorite = !prev
                        onToggleFavorite(cardItem.id, prev)
                    }
                    .padding(4.dp)
            )
        }
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2))
        ) {
            Column(Modifier.padding(4.dp)) {
                val (targetW, targetH) = targetSizePx
                val imageRequest: ImageRequest = remember(firstImageUrl, targetW, targetH) {
                    ImageRequest.Builder(context)
                        .data(firstImageUrl)
                        .size(CoilSize(targetW, targetH))
                        .build()
                }
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    placeholder = placeholderPainter,
                    error = placeholderPainter,
                    fallback = placeholderPainter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(shape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    BodyLargeText(
                        text = cardItem.title ?: "Без названия",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = cardItem.shortDescription ?: cardItem.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BodyMediumText(
                            text = dateFormatted,
                        )

                        Box {
                            Image(
                                painterResource(Res.drawable.ic_like),
                                contentDescription = "Remove from favorites",
                                Modifier
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .clickable { showDeleteDialog = true }
                                    .padding(8.dp)
                            )
                            CardLikesCounter(
                                cardItem.likes,
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            ShortlistDeleteDialog({}, { showDeleteDialog = it })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortlistDeleteDialog(confirmAction: () -> Unit, setDeleteDialogState: (Boolean) -> Unit) {
    BasicAlertDialog(
        onDismissRequest = { setDeleteDialogState(false) },
        properties = DialogProperties(),
        content = {
            Card(colors = CardDefaults.cardColors(Color(0xFFF2F2F2))) {
                Column(Modifier.padding(7.dp, 16.dp)) {
                    BodyLargeText(
                        "Вы уверены, что хотите удалить мероприятие?",
                        Modifier.fillMaxWidth(),
                        Color(0xFF8F8E94),
                        TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { confirmAction(); setDeleteDialogState(false) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color.White)
                        ) { BodyLargeText("Да", color = Color(0xFF8F8E94)) }
                        TextButton(
                            onClick = { setDeleteDialogState(false) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(Color(0xFFF2FF87))
                        ) { BodyLargeText("Нет", color = Color(0xFF8F8E94)) }
                    }
                }
            }
        },
    )
}

@Composable
fun CardLikesCounter(
    likes: Int,
    modifier: Modifier = Modifier
) {
    Text(
        getShortCountLikes(likes),
        modifier = modifier
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
            .windowInsetsPadding(WindowInsets(0, 0, 0, 0))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        fontSize = 6.sp,
        lineHeight = 8.sp,
        color = Color.Black
    )
}

@Composable
fun EmptyShortlistContent(
    onExploreEvents: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Центрируем контент пустого состояния
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Empty favorites",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Пока нет избранных событий",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onExploreEvents,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Исследовать события")
            }
        }
    }
}

@Composable
fun EmptyFilteredShortlistContent(
    onClearFilters: () -> Unit = {},
    onExploreEvents: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Центрируем контент пустого состояния с активными фильтрами
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Active filters",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Нет событий, соответствующих активным фильтрам",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка для сброса фильтров
            Button(
                onClick = onClearFilters,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Сбросить фильтры")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка для исследования событий
            Button(
                onClick = onExploreEvents,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Исследовать события")
            }
        }
    }
}

@Composable
fun ErrorShortlistContent(
    message: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Центрируем контент ошибки
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message ?: "Произошла неизвестная ошибка",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Повторить")
            }
        }
    }
}

@Composable
fun LoadingShortlistContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Центрируем индикатор загрузки
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Загрузка избранных событий...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShortlistHeader(
    isStarred: Boolean = false,
    updateStarred: () -> Unit = {},
    showFilters: Boolean = true,
    onFiltersClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(Res.drawable.ic_revert_icon_thin),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Black
        )
        AutoSizeHeadlineText(
            text = "Ваши мероприятия",
            maxLines = 1,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Звездочка для фильтрации избранных
            AnimatedContent(
                isStarred,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "StarFilterAnimation"
            ) {
                Image(
                    painterResource(if (it) Res.drawable.ic_filled_star else Res.drawable.ic_outlined_star),
                    contentDescription = if (it) "Показать все" else "Показать избранные",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(enabled = showFilters) {
                            if (showFilters) updateStarred()
                        }
                        .padding(4.dp),
                    alpha = if (showFilters) 1f else 0.3f // Делаем полупрозрачной когда неактивна
                )
            }

            // Кнопка перехода на экран фильтров
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Открыть фильтры",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = showFilters) {
                        if (showFilters) onFiltersClick()
                    }
                    .padding(4.dp),
                tint = Color.Black.copy(alpha = if (showFilters) 1f else 0.3f) // Делаем полупрозрачной когда неактивна
            )
        }
    }
}

package ru.dada.tuda.presentation.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector3D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Surface
import dadatuda.composeapp.generated.resources.Res
import dadatuda.composeapp.generated.resources.dada_like_swipe
import dadatuda.composeapp.generated.resources.tuda_dislike_swipe
import org.jetbrains.compose.resources.painterResource
import ru.dada.tuda.presentation.theme.colorAccent
import kotlin.math.abs
import kotlin.math.min

// Оптимизированные константы для анимации
private val OPTIMIZED_SPRING_SMOOTH: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

private val TripleConverter = TwoWayConverter<Triple<Float, Float, Float>, AnimationVector3D>(
    convertToVector = { AnimationVector3D(it.first, it.second, it.third) },
    convertFromVector = { Triple(it.v1, it.v2, it.v3) }
)

/**
 * Контроллер для программного управления стопкой карточек
 */
class SwipeableCardStackController {
    private var _swipeAction: ((SwipeDirection) -> Unit)? = null
    private var _undoAction: (() -> Unit)? = null
    private var _currentIndex: Int = 0
    private var _totalItems: Int = 0

    /**
     * Текущий индекс карточки в стопке
     */
    val currentIndex: Int get() = _currentIndex

    /**
     * Общее количество элементов в стопке
     */
    val totalItems: Int get() = _totalItems

    /**
     * Закончились ли карточки в стопке
     */
    val isStackFinished: Boolean get() = _currentIndex >= _totalItems

    internal fun setSwipeAction(action: (SwipeDirection) -> Unit) {
        _swipeAction = action
    }

    internal fun setUndoAction(action: () -> Unit) {
        _undoAction = action
    }

    internal fun updateIndex(newIndex: Int, totalItems: Int) {
        _currentIndex = newIndex
        _totalItems = totalItems
    }

    /**
     * Программный свайп влево
     */
    fun swipeLeft() {
        _swipeAction?.invoke(SwipeDirection.LEFT)
    }

    /**
     * Программный свайп вправо
     */
    fun swipeRight() {
        _swipeAction?.invoke(SwipeDirection.RIGHT)
    }

    /**
     * Возврат к предыдущей карточке
     */
    fun undo() {
        _undoAction?.invoke()
    }
}

/**
 * Создает и запоминает контроллер для SwipeableCardStack
 */
@Composable
fun rememberSwipeableCardStackController(): SwipeableCardStackController {
    return remember { SwipeableCardStackController() }
}

/**
 * Универсальный компонент стопки карточек с возможностью свайпа
 *
 * @param T тип данных для карточек
 * @param items список элементов для отображения
 * @param onItemSwiped callback при свайпе карточки
 * @param onLoadMore callback для подгрузки новых данных
 * @param cardContent composable функция для отображения содержимого карточки
 * @param modifier модификатор для стопки
 * @param controller контроллер для программного управления свайпами
 * @param maxVisibleCards максимальное количество видимых карточек в стопке
 * @param swipeThreshold порог для срабатывания свайпа (в пикселях)
 * @param emptyMessage сообщение при пустой стопке
 */
@Composable
fun <T> SwipeableCardStack(
    items: List<T>,
    onItemSwiped: (T, SwipeDirection) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    controller: SwipeableCardStackController? = null,
    maxVisibleCards: Int = 3,
    swipeThreshold: Float = 150f,
    emptyMessage: String = "Нет больше карточек",
    cardContent: @Composable (T) -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var programmaticSwipeDirection by remember { mutableStateOf<SwipeDirection?>(null) }
    // Стек для хранения предыдущих карточек с их направлениями свайпа
    var swipedItemsStack by remember { mutableStateOf<List<Pair<T, SwipeDirection>>>(emptyList()) }

    // Настройка контроллера для программного свайпа и возврата
    LaunchedEffect(controller, items) {
        controller?.updateIndex(currentIndex, items.size)
        controller?.setSwipeAction { direction ->
            if (currentIndex < items.size) {
                programmaticSwipeDirection = direction
            }
        }

        controller?.setUndoAction {
            if (swipedItemsStack.isNotEmpty() && currentIndex > 0) {
                currentIndex--
                swipedItemsStack = swipedItemsStack.dropLast(1)
            }
        }
    }

    // Сброс состояния программного свайпа после передачи в карточку
    LaunchedEffect(programmaticSwipeDirection) {
        if (programmaticSwipeDirection != null) {
            // Даем карточке время начать анимацию, затем сбрасываем состояние
            kotlinx.coroutines.delay(100)
            programmaticSwipeDirection = null
        }
    }

    // Упрощенная анимация текущего индекса
    val animatedCurrentIndex by animateFloatAsState(
        targetValue = currentIndex.toFloat(),
        animationSpec = OPTIMIZED_SPRING_SMOOTH,
        label = "currentIndex"
    )

    // Подгрузка данных при приближении к концу списка
    LaunchedEffect(currentIndex, items.size) {
        if (currentIndex >= items.size - 2) {
            onLoadMore()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Отображаем только видимые карточки в обратном порядке для правильного z-index
        for (i in (currentIndex + maxVisibleCards - 1) downTo currentIndex) {
            if (i < items.size) {
                val cardIndex = i - currentIndex
                val animatedCardIndex = i - animatedCurrentIndex
                val item = items[i]

                key(item.hashCode()) {
                    SwipeableCard(
                        cardIndex = cardIndex,
                        animatedCardIndex = animatedCardIndex,
                        maxVisibleCards = maxVisibleCards,
                        swipeThreshold = swipeThreshold,
                        programmaticSwipeDirection = if (cardIndex == 0) programmaticSwipeDirection else null,
                        onCardSwiped = { direction: SwipeDirection ->
                            // Сохраняем карточку и направление в стек перед вызовом callback
                            swipedItemsStack = swipedItemsStack + (item to direction)
                            onItemSwiped(item, direction)
                            currentIndex++
                            println(items)
                            println("Swiped item at index $i to $direction, currentIndex is now $currentIndex")
                        },
                        content = { cardContent(item) }
                    )
                }
            }
        }

        // Показать сообщение если карточки закончились
        if (currentIndex >= items.size) {
            EmptyStateCard(
                message = emptyMessage,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Кастомная карточка для отображения пустого состояния
 */
@Composable
private fun EmptyStateCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Фоновая декорация - стопка прозрачных карточек
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.15f),
                contentAlignment = Alignment.Center
            ) {
                repeat(4) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxSize(0.9f - index * 0.08f)
                            .alpha(0.3f - index * 0.06f),
                        shape = RectangleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {}
                }
            }
            
            // Основной контент
            Surface(
                modifier = Modifier
                    .padding(32.dp),
                shape = RectangleShape,
                color = Color.Transparent
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                    // Эмодзи для визуального интереса
                    Text(
                        text = "🎉",
                        fontSize = 64.sp,
                        modifier = Modifier.alpha(0.8f)
                    )
                    
                    Text(
                        text = message,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0.9f)
                    )
                    
                    Text(
                        text = "Возвращайтесь завтра за новыми мероприятиями",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Отдельная карточка в стопке с поддержкой свайпа по окружности
 */
@Composable
private fun SwipeableCard(
    cardIndex: Int,
    animatedCardIndex: Float,
    maxVisibleCards: Int,
    swipeThreshold: Float,
    programmaticSwipeDirection: SwipeDirection? = null,
    onCardSwiped: (SwipeDirection) -> Unit,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var isRemoving by remember { mutableStateOf(false) }
    var programmaticTarget by remember { mutableStateOf<Offset?>(null) }

    // Радиус окружности для ограничения движения
    val circularRadius = 200f

    // Обработка программного свайпа - устанавливаем целевую позицию для анимации
    LaunchedEffect(programmaticSwipeDirection) {
        programmaticSwipeDirection?.let { direction ->
            if (!isRemoving && !isDragging) {
                programmaticTarget = when (direction) {
                    SwipeDirection.LEFT -> Offset(-swipeThreshold * 3, 0f)
                    SwipeDirection.RIGHT -> Offset(swipeThreshold * 3, 0f)
                    SwipeDirection.NONE -> Offset.Zero
                }
                isRemoving = true
            }
        }
    }

    val density = LocalDensity.current
    val animatedOffset by animateOffsetAsState(
        targetValue = when {
            isDragging -> offset.copy(y = 0f)
            isRemoving && programmaticTarget != null -> programmaticTarget!!
            isRemoving -> offset.copy(x = if (offset.x > 0) 1200f else -1200f, y = 0f)
            else -> Offset.Zero
        },
        animationSpec = if (isRemoving) {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        },
        finishedListener = {
            if (isRemoving) {
                val direction = when {
                    programmaticTarget != null -> if (programmaticTarget!!.x > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                    else -> if (offset.x > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                }
                onCardSwiped(direction)
                // Сбрасываем локальные состояния, чтобы следующая карточка была интерактивной
                programmaticTarget = null
                isRemoving = false
                isDragging = false
                offset = Offset.Zero
            }
        },
        label = "offset"
    )

    val animatedStackProperties by animateValueAsState(
        targetValue = with(density) {
            Triple(
                1f - (animatedCardIndex * 0.04f), // Убираем дополнительное уменьшение при перетаскивании
                animatedCardIndex * 6f,
                if (isDragging && cardIndex == 0) 12.dp.toPx() else 6.dp.toPx()
            )
        },
        typeConverter = TripleConverter,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "stackProperties"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isRemoving) 0f else if (cardIndex < maxVisibleCards) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val swipeProgress = min(1f, abs(animatedOffset.x) / swipeThreshold)
    val swipeDirection = when {
        animatedOffset.x > 50f -> SwipeDirection.RIGHT
        animatedOffset.x < -50f -> SwipeDirection.LEFT
        else -> SwipeDirection.NONE
    }

    val isTopCard = cardIndex == 0

    Card(
        modifier = Modifier
            .fillMaxSize()
            .zIndex((maxVisibleCards - cardIndex).toFloat())
            .graphicsLayer {
                translationX = animatedOffset.x
                translationY =
                    animatedOffset.y
//                + with(density) { animatedStackProperties.second.dp.toPx() }
                rotationZ = animatedOffset.x * 0.03f // Увеличиваем угол поворота для большего отклонения
//                scaleX = animatedStackProperties.first
//                scaleY = animatedStackProperties.first
                shadowElevation = animatedStackProperties.third
                this.alpha = alpha
            }
            .then(
                if (isTopCard) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                if (abs(offset.x) > swipeThreshold) {
                                    isRemoving = true
                                } else {
                                    offset = Offset.Zero
                                }
                            },
                            onDrag = { _, dragAmount ->
                                val newOffset = offset + dragAmount
                                // Вычисляем Y координату на основе X для движения по дуге
                                val arcY = calculateArcY(newOffset.x)
                                val arcOffset = Offset(newOffset.x, arcY)
                                // Ограничиваем движение по дуге
                                offset = constrainToArc(arcOffset, circularRadius)
                            }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            content()
            if (isTopCard && swipeDirection != SwipeDirection.NONE) {
                SwipeIndicators(
                    swipeDirection = swipeDirection,
                    swipeProgress = swipeProgress,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Оптимизированные индикаторы свайпа
 */
@Composable
private fun SwipeIndicators(
    swipeDirection: SwipeDirection,
    swipeProgress: Float,
    modifier: Modifier = Modifier
) {
    when (swipeDirection) {
        SwipeDirection.RIGHT -> {
            Box(
                modifier = modifier
                    .alpha(swipeProgress * 0.8f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(colorAccent.copy(alpha = 0.3f), Color.Transparent),
                            radius = 900f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(Res.drawable.dada_like_swipe),
                    contentDescription = "Like",
                    modifier = Modifier
                        .alpha(swipeProgress)
                        .scale((0.6f + swipeProgress * 0.5f).coerceAtMost(1f))
                )
            }
        }

        SwipeDirection.LEFT -> {
            Box(
                modifier = modifier
                    .alpha(swipeProgress * 0.8f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF601C).copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            radius = 900f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(Res.drawable.tuda_dislike_swipe),
                    contentDescription = "Dislike",
                    modifier = Modifier
                        .alpha(swipeProgress)
                        .scale((0.6f + swipeProgress * 0.5f).coerceAtMost(1f))
                )
            }
        }

        else -> {}
    }
}

/**
 * Вычисляет Y координату для движения по дуге на основе X координаты
 * Использует параболическую формулу для создания естественной дуги
 */
private fun calculateArcY(x: Float, arcHeight: Float = 50f): Float {
    // Параболическая формула: y = -a * x^2 / (width^2) * height
    // где width - это примерная ширина дуги (600px - увеличено в 1.5 раза)
    val arcWidth = 30f
    val normalizedX = x / arcWidth
    return -arcHeight * normalizedX * normalizedX * 4 // Умножаем на 4 для более выраженной дуги
}

/**
 * Ограничивает позицию по дуге с заданным радиусом
 * Карточка движется по параболической дуге вместо окружности
 */
private fun constrainToArc(offset: Offset, radius: Float): Offset {
    val x = offset.x
    
    // Ограничиваем x в пределах радиуса
    val constrainedX = when {
        x > radius -> radius
        x < -radius -> -radius
        else -> x
    }
    
    // Вычисляем y по параболической формуле: y = -(x²) / (2 * radius) + radius/4
    // Это создает дугу, которая опускается вниз при движении влево/вправо
    val normalizedX = constrainedX / radius * 2 // нормализуем x к диапазону [-1, 1]
    val y = -(normalizedX * normalizedX) * radius * 0.5f + radius * 0.5f
    
    return Offset(constrainedX, y)
}

/**
 * Enum для направления свайпа
 */
enum class SwipeDirection {
    LEFT, RIGHT, NONE
}

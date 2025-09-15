package ru.dada.tuda.presentation.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Компонент текста с автоматическим подбором размера шрифта
 *
 * @param text Текст для отображения
 * @param modifier Модификатор для стилизации
 * @param color Цвет текста
 * @param fontSize Начальный размер шрифта
 * @param fontWeight Насыщенность шрифта
 * @param fontFamily Семейство шрифтов
 * @param textAlign Выравнивание текста
 * @param textDecoration Декорация текста (подчеркивание, зачеркивание)
 * @param maxLines Максимальное количество строк
 * @param minFontSize Минимальный размер шрифта
 * @param maxFontSize Максимальный размер шрифта (TextUnit.Unspecified для неограниченного роста)
 * @param stepGranularityRatio Шаг изменения размера шрифта (от 0.1 до 1.0)
 * @param overflow Поведение при переполнении
 * @param style Стиль текста
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    textDecoration: TextDecoration? = null,
    maxLines: Int = Int.MAX_VALUE,
    minFontSize: TextUnit = 12.sp,
    maxFontSize: TextUnit = 24.sp,
    stepGranularityRatio: Float = 0.9f,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = LocalTextStyle.current
) {
    BoxWithConstraints(modifier = modifier) {
        var currentFontSize by remember(text, this.maxWidth, this.maxHeight) {
            mutableStateOf(fontSize)
        }

        val textStyle = style.copy(
            fontSize = currentFontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            color = color
        )

        // Улучшенная логика подбора размера шрифта с возможностью увеличения
        LaunchedEffect(text, this.maxWidth, this.maxHeight) {
            val availableWidth = this@BoxWithConstraints.maxWidth.value
            val availableHeight = this@BoxWithConstraints.maxHeight.value

            if (availableWidth <= 0) return@LaunchedEffect

            var bestFontSize = minFontSize

            // Определяем максимальный размер для поиска
            val searchMaxSize = if (maxFontSize == TextUnit.Unspecified) {
                // Если максимум не задан, используем разумное ограничение на основе доступного пространства
                val maxPossibleSize = kotlin.math.min(availableWidth, if (availableHeight > 0) availableHeight else availableWidth) / 2f
                kotlin.math.max(minFontSize.value, maxPossibleSize).sp
            } else {
                maxFontSize
            }

            // Бинарный поиск оптимального размера шрифта
            var minSize = minFontSize.value
            var maxSize = searchMaxSize.value

            while (maxSize - minSize > 0.5f) {
                val midSize = (minSize + maxSize) / 2f
                val testFontSize = midSize.sp

                // Проверяем, помещается ли текст с данным размером шрифта
                val estimatedCharWidth = testFontSize.value * 0.6f
                val estimatedLineHeight = testFontSize.value * 1.2f
                val textLength = text.length
                val estimatedTextWidth = textLength * estimatedCharWidth

                val linesNeeded = kotlin.math.ceil(estimatedTextWidth / availableWidth).toInt()
                val totalHeight = linesNeeded * estimatedLineHeight

                val fitsWidth = linesNeeded <= maxLines
                val fitsHeight = availableHeight <= 0 || totalHeight <= availableHeight

                if (fitsWidth && fitsHeight) {
                    bestFontSize = testFontSize
                    minSize = midSize // Пробуем увеличить размер
                } else {
                    maxSize = midSize // Уменьшаем размер
                }
            }

            currentFontSize = bestFontSize
        }

        Text(
            text = text,
            style = textStyle,
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines
        )
    }
}

/**
 * Упрощенная версия AutoSizeText с предустановленными параметрами для заголовков
 */
@Composable
fun AutoSizeHeadlineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = 2,
) {
    AutoSizeText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
//        fontFamily = fontFamily,
        textAlign = textAlign,
        maxLines = maxLines,
        minFontSize = 16.sp,
        maxFontSize = TextUnit.Unspecified,
    )
}

/**
 * Упрощенная версия AutoSizeText с предустановленными параметами для основного текста
 */
@Composable
fun AutoSizeBodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = 3,
) {
    AutoSizeTextAnimated(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
//        fontFamily = fontFamily,
        textAlign = textAlign,
        maxLines = maxLines,
        minFontSize = 12.sp,
        maxFontSize = 20.sp,
        stepGranularityRatio = 0.9f
    )
}

/**
 * AutoSizeText с анимированным изменением размера шрифта
 */
@Composable
fun AutoSizeTextAnimated(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    textDecoration: TextDecoration? = null,
    maxLines: Int = Int.MAX_VALUE,
    minFontSize: TextUnit = 12.sp,
    maxFontSize: TextUnit = 20.sp,
    stepGranularityRatio: Float = 0.9f,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = LocalTextStyle.current
) {
    BoxWithConstraints(modifier = modifier) {
        var targetFontSize by remember(text, this.maxWidth, this.maxHeight) {
            mutableStateOf(fontSize)
        }

        // Анимация изменения размера шрифта
        val animatedFontSize by androidx.compose.animation.core.animateFloatAsState(
            targetValue = targetFontSize.value,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
            ),
            label = "fontSizeAnimation"
        )

        LaunchedEffect(text, this.maxWidth, this.maxHeight) {
            val availableWidth = this@BoxWithConstraints.maxWidth.value
            val availableHeight = this@BoxWithConstraints.maxHeight.value

            if (availableWidth <= 0) return@LaunchedEffect

            // Используем тот же алгоритм бинарного поиска для анимированной версии
            var testFontSize = maxFontSize
            var bestFontSize = minFontSize

            // Бинарный поиск оптимального размера шрифта
            var minSize = minFontSize.value
            var maxSize = maxFontSize.value

            while (maxSize - minSize > 0.5f) {
                val midSize = (minSize + maxSize) / 2f
                testFontSize = midSize.sp

                // Проверяем, помещается ли текст с данным размером шрифта
                val estimatedCharWidth = testFontSize.value * 0.6f
                val estimatedLineHeight = testFontSize.value * 1.2f
                val textLength = text.length
                val estimatedTextWidth = textLength * estimatedCharWidth

                val linesNeeded = kotlin.math.ceil(estimatedTextWidth / availableWidth).toInt()
                val totalHeight = linesNeeded * estimatedLineHeight

                val fitsWidth = linesNeeded <= maxLines
                val fitsHeight = availableHeight <= 0 || totalHeight <= availableHeight

                if (fitsWidth && fitsHeight) {
                    bestFontSize = testFontSize
                    minSize = midSize // Пробуем увеличить размер
                } else {
                    maxSize = midSize // Уменьшаем размер
                }
            }

            targetFontSize = bestFontSize
        }

        val textStyle = style.copy(
            fontSize = animatedFontSize.sp,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            color = color
        )

        Text(
            text = text,
            style = textStyle,
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines
        )
    }
}

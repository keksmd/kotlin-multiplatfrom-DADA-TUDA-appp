package ru.dada.tuda.presentation.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoSizeText1(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    minFontSize: TextUnit = 12.sp,
    maxFontSize: TextUnit = TextUnit.Unspecified,
    stepGranularityTextSize: TextUnit = 1.sp,
    maxLines: Int = Int.MAX_VALUE,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    style: TextStyle = LocalTextStyle.current,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val density = LocalDensity.current

//    BoxWithConstraints(modifier = modifier) {
//        var shrunkFontSize by remember(text, maxWidth, maxHeight, maxLines) {
//            mutableStateOf(
//                if (maxFontSize != TextUnit.Unspecified) maxFontSize
//                else if (style.fontSize != TextUnit.Unspecified) style.fontSize
//                else 16.sp
//            )
//        }
//
//        // Простая реализация: просто используем Text с текущим размером шрифта
//        // В будущем можно добавить более сложную логику автоматического изменения размера
//        Text(
//            text = text,
//            color = color,
//            fontSize = shrunkFontSize.coerceAtLeast(minFontSize),
//            fontStyle = fontStyle,
//            fontWeight = fontWeight,
//            fontFamily = fontFamily,
//            letterSpacing = style.letterSpacing,
//            textDecoration = textDecoration,
//            textAlign = textAlign,
//            lineHeight = lineHeight,
//            overflow = TextOverflow.Ellipsis,
//            softWrap = true,
//            maxLines = maxLines,
//            onTextLayout = onTextLayout,
//            style = style
//        )
//    }
}


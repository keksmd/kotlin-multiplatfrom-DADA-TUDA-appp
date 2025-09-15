package ru.dada.tuda.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

/**
 * Компоненты текста для различных размеров Display
 */

@Composable
fun DisplayLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.displayLarge.fontFamily
        )
    )
}

@Composable
fun DisplayMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.displayMedium.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.displayMedium.fontFamily
        )
    )
}

@Composable
fun DisplaySmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.displaySmall.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.displaySmall.fontFamily
        )
    )
}

/**
 * Компоненты текста для заголовков Headline
 */

@Composable
fun HeadlineLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.headlineLarge.fontFamily
        )
    )
}

@Composable
fun HeadlineMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.headlineMedium.fontFamily
        )
    )
}

@Composable
fun HeadlineSmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.headlineSmall.fontFamily
        )
    )
}

/**
 * Компоненты текста для заголовков Title
 */

@Composable
fun TitleLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Medium,
            fontFamily = fontFamily ?: MaterialTheme.typography.titleLarge.fontFamily
        )
    )
}

@Composable
fun TitleMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.titleMedium.fontFamily
        )
    )
}

@Composable
fun TitleSmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.titleSmall.fontFamily
        )
    )
}

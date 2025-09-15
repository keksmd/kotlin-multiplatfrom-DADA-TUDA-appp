package ru.dada.tuda.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

/**
 * Компоненты текста для основного контента Body
 */

@Composable
fun BodyLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = fontWeight ?: MaterialTheme.typography.bodyLarge.fontWeight,
            fontFamily = fontFamily ?: MaterialTheme.typography.bodyLarge.fontFamily
        )
    )
}

@Composable
fun BodyMediumText(
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
        style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.bodyMedium.fontFamily
        )
    )
}

@Composable
fun BodySmallText(
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
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.bodySmall.fontFamily
        )
    )
}

/**
 * Компоненты текста для меток и подписей Label
 */

@Composable
fun LabelLargeText(
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
        style = MaterialTheme.typography.labelLarge.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.labelLarge.fontFamily
        )
    )
}

@Composable
fun LabelMediumText(
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
        style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.labelMedium.fontFamily
        )
    )
}

@Composable
fun LabelSmallText(
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
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = fontFamily ?: MaterialTheme.typography.labelSmall.fontFamily
        )
    )
}
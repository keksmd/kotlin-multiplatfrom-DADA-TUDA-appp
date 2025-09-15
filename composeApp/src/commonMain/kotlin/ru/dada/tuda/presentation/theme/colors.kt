package ru.dada.tuda.presentation.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val colorAccent = Color(0xFFF2FF87)

val AppColorScheme = lightColorScheme(
//    primary = colorAccent,
//    onPrimaryContainer = colorAccent,
//    secondary = colorAccent,
//    onSecondaryContainer = colorAccent,
    surfaceContainer = Color(0xFF191919), // Dark surface
    error = Color(0xFFFF0000), // Red for error
    onError = Color.White,
    background = Color(0xFF191919), // White background
    onBackground = Color.White
)
package ru.dada.tuda.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = AppTypography(),
        colorScheme = AppColorScheme,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    AppTheme(content = content)
}

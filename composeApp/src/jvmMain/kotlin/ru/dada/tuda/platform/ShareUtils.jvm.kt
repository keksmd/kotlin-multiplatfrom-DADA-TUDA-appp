package ru.dada.tuda.platform

import androidx.compose.runtime.Composable

@Composable
actual fun shareText(text: String): Boolean {
    // Desktop реализация не поддержана (нужно добавить Desktop.getDesktop() и т.п.)
    return false
}


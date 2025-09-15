package ru.dada.tuda.platform

import androidx.compose.runtime.Composable

@Composable
actual fun openUrl(url: String?): Boolean {
    // Desktop реализация может быть добавлена позже (например, через Desktop.getDesktop().browse)
    return false
}


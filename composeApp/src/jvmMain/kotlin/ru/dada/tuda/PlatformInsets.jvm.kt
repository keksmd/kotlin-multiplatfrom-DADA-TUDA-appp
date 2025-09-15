package ru.dada.tuda

import androidx.compose.ui.Modifier

// Desktop/JVM: не добавляем системные отступы
actual fun Modifier.platformSystemBars(): Modifier = this


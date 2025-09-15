package ru.dada.tuda

import androidx.compose.ui.Modifier

// Платформенно-специфичный модификатор паддингов системных баров.
// iOS: только верх (статус бар)
// Остальные платформы: верх + низ (status + navigation / gesture)
expect fun Modifier.platformSystemBars(): Modifier


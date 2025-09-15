package ru.dada.tuda

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier

actual fun Modifier.platformSystemBars(): Modifier = this.systemBarsPadding()
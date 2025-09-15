package ru.dada.tuda.platform

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard
import ru.dada.tuda.domain.util.PlatformContext

actual fun shareText(text: String, platformContext: PlatformContext): Boolean {
    return try {
        UIPasteboard.generalPasteboard().setString(text)
        true
    } catch (_: Throwable) {
        false
    }
}
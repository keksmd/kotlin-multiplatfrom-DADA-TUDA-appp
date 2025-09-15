package ru.dada.tuda.platform

import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import ru.dada.tuda.domain.util.PlatformContext

actual fun openUrl(url: String?, platformContext: PlatformContext): Boolean {
    if (url.isNullOrBlank()) return false
    return try {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        val app = UIApplication.sharedApplication
        if (app.canOpenURL(nsUrl)) {
            app.openURL(nsUrl)
            true
        } else false
    } catch (t: Throwable) {
        false
    }
}


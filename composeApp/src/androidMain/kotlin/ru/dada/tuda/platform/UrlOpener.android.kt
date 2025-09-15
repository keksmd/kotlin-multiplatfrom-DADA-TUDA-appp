package ru.dada.tuda.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import org.koin.compose.koinInject
import ru.dada.tuda.domain.util.PlatformContext

actual fun openUrl(url: String?, platformContext: PlatformContext): Boolean {
    if (url.isNullOrBlank()) return false
    val context: Context = platformContext.context
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    // Проверяем, что есть Activity для обработки
    val resolveInfo = intent.resolveActivity(context.packageManager)
    if (resolveInfo != null) {
        context.startActivity(intent)
        return true
    }

    return false
}


package ru.dada.tuda.platform

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ru.dada.tuda.domain.util.PlatformContext

actual fun shareText(text: String, platformContext: PlatformContext): Boolean {
    val context = platformContext.context
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val shareIntent = Intent.createChooser(sendIntent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(shareIntent)
    return true
}


package ru.dada.tuda.domain.util

import platform.Foundation.NSUserDefaults

private const val KEY_TOKEN = "auth_token"

actual class TokenManager {
    private val defaults: NSUserDefaults by lazy { NSUserDefaults.standardUserDefaults() }

    actual fun init(platformContext: PlatformContext?) { /* no-op для iOS */ }

    actual fun saveToken(token: String?) {
        if (token == null) {
            defaults.removeObjectForKey(KEY_TOKEN)
        } else {
            defaults.setObject(token, KEY_TOKEN)
        }
        defaults.synchronize()
    }

    actual fun getToken(): String? = defaults.stringForKey(KEY_TOKEN)

    actual fun clearToken() {
        defaults.removeObjectForKey(KEY_TOKEN)
        defaults.synchronize()
    }
}

actual class PlatformContext // пустая заглушка для iOS


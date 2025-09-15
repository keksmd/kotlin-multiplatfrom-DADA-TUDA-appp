package ru.dada.tuda.domain.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val PREFS_NAME = "secure_token_prefs"
private const val KEY_TOKEN = "auth_token"

actual class TokenManager {
    // volatile чтобы изменения сразу были видны из разных потоков
    @Volatile private var prefs: SharedPreferences? = null

    actual fun init(platformContext: PlatformContext?) {
        if (prefs != null) return
        val ctx = platformContext?.context ?: return
        synchronized(this) {
            if (prefs == null) {
                prefs = createEncryptedPrefs(ctx)
            }
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // fallback (нешифрованно, но гарантирует работу если что-то пошло не так)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun ensurePrefs(): SharedPreferences {
        val current = prefs
        if (current != null) return current
        throw IllegalStateException("TokenManager.init(context) must be called on Android before usage")
    }

    actual fun saveToken(token: String?) {
        val p = ensurePrefs()
        p.edit().apply {
            if (token == null) remove(KEY_TOKEN) else putString(KEY_TOKEN, token)
        }.apply()
    }

    actual fun getToken(): String? = ensurePrefs().getString(KEY_TOKEN, null)

    actual fun clearToken() {
        ensurePrefs().edit().remove(KEY_TOKEN).apply()
    }
}

actual class PlatformContext internal constructor(internal val context: Context)

// Утилита для удобного создания PlatformContext из Android Context (можно использовать в Application)
fun Context.asPlatformContext(): PlatformContext = PlatformContext(this)


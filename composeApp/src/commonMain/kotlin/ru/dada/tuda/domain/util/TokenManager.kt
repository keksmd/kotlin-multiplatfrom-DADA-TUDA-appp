package ru.dada.tuda.domain.util

/**
 * Мультиплатформенный менеджер токена.
 * Android: использует EncryptedSharedPreferences (нужен вызов init с контекстом).
 * iOS: использует NSUserDefaults, init можно не вызывать.
 */
expect class TokenManager {
    /**
     * Инициализация (нужна только на Android). На других платформах – no-op.
     * Передавайте platformContext только на Android.
     */
    fun init(platformContext: PlatformContext? = null)

    fun saveToken(token: String?)
    fun getToken(): String?
    fun clearToken()
}

/**
 * Платформенно-специфичный контекст (Android = Context, iOS = заглушка).
 */
expect class PlatformContext

package ru.dada.tuda.platform

import ru.dada.tuda.domain.util.PlatformContext

/**
 * Кроссплатформенный шэринг текста (Android: системный share sheet, iOS: копирование в буфер/ (можно доработать до UIActivityViewController)).
 * @return true если операция выполнена.
 */
expect fun shareText(text: String, platformContext: PlatformContext): Boolean


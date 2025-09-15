package ru.dada.tuda.platform

import ru.dada.tuda.domain.util.PlatformContext


/**
 * Кроссплатформенная функция открытия внешней ссылки в системном браузере.
 * @return true если запуск произведён, иначе false
 */
expect fun openUrl(url: String?, platformContext: PlatformContext): Boolean
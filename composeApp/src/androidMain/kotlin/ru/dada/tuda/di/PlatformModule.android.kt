package ru.dada.tuda.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.dada.tuda.domain.util.PlatformContext
import ru.dada.tuda.domain.util.TokenManager
import ru.dada.tuda.domain.util.asPlatformContext

actual val platformModule = module {
    // Android реализация TokenManager с инициализацией через Context
    single { androidContext().asPlatformContext() }
    single { TokenManager().apply { init(get()) } }
}


package ru.dada.tuda.di

import org.koin.dsl.module
import ru.dada.tuda.domain.util.PlatformContext
import ru.dada.tuda.domain.util.TokenManager

actual val platformModule = module {
    // iOS реализация TokenManager (init noop)
    single { PlatformContext() }
    single { TokenManager() }
}


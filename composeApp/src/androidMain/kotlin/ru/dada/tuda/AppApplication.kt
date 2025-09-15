package ru.dada.tuda

import android.app.Application
import org.koin.android.ext.koin.androidContext
import ru.dada.tuda.di.initKoin

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация Koin + платформенный модуль (TokenManager)
        initKoin {
            androidContext(this@AppApplication)
        }
    }
}


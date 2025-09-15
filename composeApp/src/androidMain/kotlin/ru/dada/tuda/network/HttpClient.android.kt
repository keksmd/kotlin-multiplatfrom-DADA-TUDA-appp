package ru.dada.tuda.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual val httpClientEngineFactory: HttpClientEngineFactory<*>
    get() = CIO
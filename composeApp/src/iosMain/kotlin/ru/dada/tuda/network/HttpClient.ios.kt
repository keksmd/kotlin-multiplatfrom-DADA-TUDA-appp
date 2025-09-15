package ru.dada.tuda.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual val httpClientEngineFactory: HttpClientEngineFactory<*>
    get() = Darwin
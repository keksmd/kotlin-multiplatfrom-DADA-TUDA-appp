package ru.dada.tuda.network

import io.ktor.client.engine.HttpClientEngineFactory

expect val httpClientEngineFactory: HttpClientEngineFactory<*>
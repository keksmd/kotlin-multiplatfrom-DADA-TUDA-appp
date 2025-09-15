package ru.dada.tuda

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
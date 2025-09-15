package ru.dada.tuda.domain.util

// KMP expect логгер
expect object KmpLog {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String)
}

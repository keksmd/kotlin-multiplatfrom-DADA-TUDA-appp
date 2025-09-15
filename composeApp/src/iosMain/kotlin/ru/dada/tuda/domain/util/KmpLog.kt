package ru.dada.tuda.domain.util

actual object KmpLog {
    actual fun d(tag: String, message: String) {
        println("[D][" + tag + "] " + message)
    }
    actual fun e(tag: String, message: String) {
        println("[E][" + tag + "] " + message)
    }
}


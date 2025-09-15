package ru.dada.tuda.domain.util

import android.util.Log

actual object KmpLog {
    actual fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
    actual fun e(tag: String, message: String) {
        Log.e(tag, message)
    }
}

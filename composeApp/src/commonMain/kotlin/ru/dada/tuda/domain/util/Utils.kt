package ru.dada.tuda.domain.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

// Форматы входа ожидаются только двух видов:
// 1) yyyy-MM-dd'T'HH:mm:ss
// 2) yyyy-MM-dd HH:mm:ss
// (секунды обязательны, миллисекунд нет). Если формат не совпадает – возвращается "" (или null для getLocalDateTime).
// Используем ручной парсинг, т.к. kotlinx-datetime не предоставляет паттернового парсинга как java.time.*.

private const val EXPECTED_LENGTH = 19 // "yyyy-MM-ddTHH:mm:ss" или с пробелом

private fun parseLocalDateTimeOrNull(raw: String): LocalDateTime? {
    if (raw.length != EXPECTED_LENGTH) return null
    val separatorDate1 = raw[4]
    val separatorDate2 = raw[7]
    if (separatorDate1 != '-' || separatorDate2 != '-') return null

    val dateTimeSep = raw[10]
    if (dateTimeSep != 'T' && dateTimeSep != ' ') return null

    val timeSep1 = raw[13]
    val timeSep2 = raw[16]
    if (timeSep1 != ':' || timeSep2 != ':') return null

    return try {
        val year = raw.substring(0, 4).toInt()
        val month = raw.substring(5, 7).toInt()
        val day = raw.substring(8, 10).toInt()
        val hour = raw.substring(11, 13).toInt()
        val minute = raw.substring(14, 16).toInt()
        val second = raw.substring(17, 19).toInt()
        LocalDateTime(year, month, day, hour, minute, second, 0)
    } catch (_: Throwable) {
        null
    }
}

private fun LocalDateTime.formatTwoLines(): String =
    buildString(11) { // "dd.MM\nHH:mm" => длина 11 с переносом
        append(dayOfMonth.toString().padStart(2, '0'))
        append('.')
        append(monthNumber.toString().padStart(2, '0'))
        append('\n')
        append(hour.toString().padStart(2, '0'))
        append(':')
        append(minute.toString().padStart(2, '0'))
    }

private fun LocalDateTime.formatSingleLine(): String =
    buildString(16) { // "dd-MM-yyyy HH:mm" => длина 16
        append(dayOfMonth.toString().padStart(2, '0'))
        append('-')
        append(monthNumber.toString().padStart(2, '0'))
        append('-')
        append(year)
        append(' ')
        append(hour.toString().padStart(2, '0'))
        append(':')
        append(minute.toString().padStart(2, '0'))
    }

fun String?.toFormattedTwoLinesDateTime(): String {
    if (this == null) return "null"
    val ldt = parseLocalDateTimeOrNull(this) ?: return ""
    return ldt.formatTwoLines()
}

fun String?.toFormattedDateTime(): String {
    if (this == null) return "null"
    val ldt = parseLocalDateTimeOrNull(this) ?: return ""
    return ldt.formatSingleLine()
}

fun String?.getLocalDateTime(): LocalDateTime? {
    if (this == null) return null
    return parseLocalDateTimeOrNull(this)
}

// Format functions for UI display
private fun LocalDateTime.formatDateOnly(): String =
    buildString(10) { // "dd.MM.yyyy" => length 10
        append(dayOfMonth.toString().padStart(2, '0'))
        append('.')
        append(monthNumber.toString().padStart(2, '0'))
        append('.')
        append(year)
    }

// Helper function to format milliseconds to dd.MM.yyyy format
@OptIn(ExperimentalTime::class)
fun Long.formatDateFromMillis(): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return localDateTime.formatDateOnly()
}

// Helper function to parse dd.MM.yyyy format to milliseconds
@OptIn(ExperimentalTime::class)
fun String.parseDateToMillis(): Long? {
    if (length != 10) return null
    val parts = split(".")
    if (parts.size != 3) return null
    
    return try {
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()
        val localDateTime = LocalDateTime(year, month, day, 0, 0)
        localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (e: Exception) {
        null
    }
}
package ru.dada.tuda.domain.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


/** DataTime is class for work with times and dates **/
@Serializable
@OptIn(ExperimentalTime::class)
open class DataTime(
    var year: Int = 0,
    var mouth: Int = 0,
    var dayOfMonth: Int = 0,
    private var hour: Int = 0,
    private var minute: Int = 0,
    var dayOfWeek: Int = 0
) {
    constructor(localDateTime: LocalDateTime) : this(
        localDateTime.year,
        localDateTime.month.number,
        localDateTime.day,
        localDateTime.hour,
        localDateTime.minute,
        localDateTime.dayOfWeek.isoDayNumber,
    )

    constructor(string: String) : this() {
        string.split("T").let {
            it[0].split("-").let { date ->
                year = date[0].toInt()
                mouth = date[1].toInt()
                dayOfMonth = date[2].toInt()
            }

            it[1].split(":").let { time ->
                hour = time[0].toInt()
                minute = time[1].toInt()
            }
        }
    }

    fun getTime() = "${if (hour > 9) hour else "0$hour"}:" + if (minute > 9) "$minute" else "0$minute"

    /** Return tomorrow/today/yesterday and day, mouth and year **/
    @OptIn(ExperimentalTime::class)
    fun getDate(): String {
        val localDateTime = LocalDateTime(year, mouth, dayOfMonth, hour, minute)
            .toInstant(TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())

        var string = ""
        val date = now()
        if (date.mouth == localDateTime.month.number && date.year == localDateTime.year) {
            when (date.dayOfMonth - localDateTime.dayOfMonth) {
                -1 -> string += "Завтра"
                0 -> string += "Сегодня"
                1 -> string += "Вчера"
                else -> {
                    string += dayOfMonth.toString()
                    string += " "
                    string += getMouthForTime(mouth)
                }
            }
        } else {
            string += dayOfMonth.toString()
            string += " "
            string += getMouthForTime(mouth)

            if (date.year != year) {
                string += " "
                string += year
            }
        }

        return string
    }

    /** Return day of week, day of mouth and mouth **/
    fun getDateForTimeTable() =
        "${getDayOfWeekText()}, $dayOfMonth ${getMouthForTime(mouth).lowercase()}"

    /** Return date for timetable **/
    fun forTimeTable() = "${mouth}-${dayOfMonth}-${year}"

    fun getIsoFormat() =
        "$year-${if (mouth > 9) mouth else "0$mouth"}-${if (dayOfMonth > 9) dayOfMonth else "0$dayOfMonth"}"

    /** Return date with time **/
    fun getDateAndTime() = getDate() + ", " + getTime()

    /** Return tomorrow day **/
    fun tomorrow() = DataTime(
        LocalDateTime(year, mouth, dayOfMonth, hour, minute)
            .toInstant(TimeZone.currentSystemDefault())
            .plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
    )

    /** Return day of next day of current **/
    fun goToNNextDay(n: Int) = DataTime(
        LocalDateTime(year, mouth, dayOfMonth, hour, minute)
            .toInstant(TimeZone.currentSystemDefault())
            .plus(n, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
    )

    fun getWeek() = LocalDate(year, mouth, dayOfMonth).getWeekNumber()

    fun getNextWeek() = LocalDateTime(year, mouth, dayOfMonth, hour, minute)
        .toInstant(TimeZone.currentSystemDefault())
        .plus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        .toLocalDateTime(TimeZone.currentSystemDefault()).getWeekNumber()

    /** Return shortcut day of week **/
    fun getShortcutDayOfWeek() = when (dayOfWeek) {
        DayOfWeek.MONDAY.isoDayNumber -> "пн"
        DayOfWeek.TUESDAY.isoDayNumber -> "вт"
        DayOfWeek.WEDNESDAY.isoDayNumber -> "ср"
        DayOfWeek.THURSDAY.isoDayNumber -> "чт"
        DayOfWeek.FRIDAY.isoDayNumber -> "пт"
        DayOfWeek.SATURDAY.isoDayNumber -> "сб"
        DayOfWeek.SUNDAY.isoDayNumber -> "вс"
        else -> ""
    }

    /** Return day of week as text **/
    private fun getDayOfWeekText() = when (dayOfWeek) {
        DayOfWeek.MONDAY.isoDayNumber -> "Понедельник"
        DayOfWeek.TUESDAY.isoDayNumber -> "Вторник"
        DayOfWeek.WEDNESDAY.isoDayNumber -> "Среда"
        DayOfWeek.THURSDAY.isoDayNumber -> "Четверг"
        DayOfWeek.FRIDAY.isoDayNumber -> "Пятница"
        DayOfWeek.SATURDAY.isoDayNumber -> "Суббота"
        DayOfWeek.SUNDAY.isoDayNumber -> "Воскресенье"
        else -> ""
    }

    override fun toString(): String {
        return "$dayOfMonth.$mouth.$year"
    }

    fun getTimeInMilliSeconds() =
        LocalDateTime(year, mouth, dayOfMonth, hour, minute).toInstant(TimeZone.currentSystemDefault()).epochSeconds

    fun endPair(duration: Double = 1.0) = DataTime(
        LocalDateTime(year, mouth, dayOfMonth, hour, minute)
            .toInstant(TimeZone.currentSystemDefault())
            .plus((90 * duration).toInt(), DateTimeUnit.MINUTE, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
    )

    fun getDayAndMouth() = "$dayOfMonth ${getMouthForTime(mouth)}"

    fun getStartAndEndTime(duration: Double = 1.0) = getTime() + " - " + endPair(duration).getTime()

    /** Function for compare to date **/
    operator fun compareTo(other: DataTime): Int {
        if (this.year > other.year) return 1
        if (this.year < other.year) return -1
        if (this.mouth > other.mouth) return 1
        if (this.mouth < other.mouth) return -1
        if (this.dayOfMonth > other.dayOfMonth) return 1
        if (this.dayOfMonth < other.dayOfMonth) return -1
        return 0
    }


    /** Static fields and functions **/
    companion object {
        /** Return today date and current time **/
        fun now() = DataTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))

        fun getStartThisWeek() =
            DataTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                .let {
                    it.toInstant(TimeZone.currentSystemDefault()).plus(
                        -it.dayOfWeek.ordinal,
                        DateTimeUnit.DAY,
                        TimeZone.currentSystemDefault()
                    ).toLocalDateTime(TimeZone.currentSystemDefault())
                })

        fun getUTCTime() = Clock.System.now().toLocalDateTime(TimeZone.UTC).let {
            "${it.year}-${getMonth(it.month.number)}-${it.day}T${it.hour}:${it.minute}:${it.second}.${it.nanosecond / 1000}"
        }

        fun getTime() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            "${it.year}-${getMonth(it.month.number)}-${it.day}T${it.hour}:${it.minute}:${it.second}.${it.nanosecond / 1000}"
        }

        private fun getMonth(i: Int) = if (i > 9) "$i" else "0$i"

        /** Parse string from timetable to date **/
        fun parseFromTimeTable(date: String) = DataTime(LocalDate.parse(date).atTime(0, 0))

        fun parse(date: String) = DataTime(LocalDateTime.parse(date))

        /** Parse string from request to date **/
        fun parseFromRequestTimeTable(date: String) = DataTime(
            LocalDate.parse(
                date.split("-")
                    .let { it[2] + "-" + (if (it[0].length == 1) "0" else "") + it[0] + "-" + (if (it[1].length == 1) "0" else "") + it[1] })
                .atTime(0, 0)
        )

        /** Return mouth as text for time in the genitive **/
        fun getMouthForTime(mouth: Int) = when (mouth) {
            1 -> "Января"
            2 -> "Февраля"
            3 -> "Марта"
            4 -> "Апреля"
            5 -> "Мая"
            6 -> "Июня"
            7 -> "Июля"
            8 -> "Августа"
            9 -> "Сентября"
            10 -> "Октября"
            11 -> "Ноября"
            12 -> "Декабря"
            else -> ""
        }

        fun getStartDayOfWeek(weekNumber: Int): DataTime {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val startDayOfYear = LocalDateTime(year = today.year, 1, 1, 0, 0)
            val firstDayOfFirstWeek = when {
                startDayOfYear.dayOfWeek.isoDayNumber <= 4 -> startDayOfYear.toInstant(TimeZone.currentSystemDefault())
                    .minus(startDayOfYear.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

                else -> startDayOfYear.toInstant(TimeZone.currentSystemDefault())
                    .plus(8 - startDayOfYear.dayOfWeek.isoDayNumber, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

            }
            return DataTime(
                firstDayOfFirstWeek.plus(7 * (weekNumber - 1), DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            )
        }
    }
}


fun LocalDate.getWeekNumber(): Int {
    val firstDayOfYear = LocalDateTime(year, 1, 1, 0, 0)
    val daysFromFirstDay = dayOfYear - firstDayOfYear.dayOfYear
    val firstDayOfYearDayOfWeek = firstDayOfYear.dayOfWeek.isoDayNumber
    val adjustment = when {
        firstDayOfYearDayOfWeek <= 4 -> firstDayOfYearDayOfWeek - 1
        else -> -(8 - firstDayOfYearDayOfWeek)
    }
    return (daysFromFirstDay + adjustment) / 7 + 1
}

fun LocalDateTime.getWeekNumber(): Int {
    return LocalDate(year, month, dayOfMonth).getWeekNumber()
}
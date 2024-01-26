package com.coderGtm.yantra.commands.time

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

fun currentTimeWithOffset(offsetHours: Int, offsetMinutes: Int = 0): String {
    val timeZoneId =
        "GMT${if (offsetHours >= 0) "+" else ""}$offsetHours:${"%02d".format(abs(offsetMinutes))}"
    val timeZone = TimeZone.getTimeZone(timeZoneId)
    val calendar = Calendar.getInstance(timeZone)
    val dateFormat = SimpleDateFormat("HH:mm:ss E d/M/y", Locale.getDefault())
    dateFormat.timeZone = timeZone

    return dateFormat.format(calendar.time)
}

fun getNormalTimeString(inputTime: String): Array<Int> {
    val direction = inputTime.first()
    val ts = inputTime.drop(1)
    var hrs = ts.split(":")[0].toInt()
    val mins = ts.split(":")[1].toInt()

    if (direction == '-') hrs *= -1

    return arrayOf(hrs, mins)
}

fun isValidString(str: String): Boolean {
    val ts = str.trim()
    val direction = ts.first()
    if (direction != '+' && direction != '-') {
        return false
    }
    val sp = ts.split(":")
    if (sp.size != 2) {
        return false
    }
    val hr = sp[0].toIntOrNull()
    val min = sp[1].toIntOrNull()

    if (hr == null || min == null) {
        return false
    }

    if (hr < -11 || hr > 13 || min < 0 || min > 59) {
        return false
    }
    return true
}
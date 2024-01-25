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
    var time = inputTime

    if (time.length > 7) {
        return arrayOf(0, 0)
    }

    if (time.contains(" ")){
        time = time.replace(" ", "")
    }

    if (time.first() == '+'){
        time = time.drop(1)
    }

    if (time.length > 1) {
        if (time.first() == '0') {
            time = time.drop(1)
        } else if (time.first() == '-' && time[1] == '0' && time.length > 2) {
            time = removeNthChar(time, 1)
        }

        if (time.isEmpty()) {
            return arrayOf(0, 0)
        }

        return if (time.contains(":")){
            var arrayOfTime = time.split(":").toMutableList()

            if (arrayOfTime[1].length == 1) {
                arrayOfTime[1] += "0"
            }

            if (arrayOfTime[0].toInt() > 13 || arrayOfTime[0].toInt() < -11) {
                arrayOfTime[0] = "0"
            }

            arrayOf(arrayOfTime[0].toInt(), arrayOfTime[1].toInt())
        } else {
            arrayOf(time.toInt(), 0)
        }
    } else {
        if (time.toInt() > 13 || time.toInt() < -11) {
            time = "0"
        }

        return arrayOf(time.toInt(), 0)
    }
}

fun isCorrectString(str: String): Boolean {
    for (symbol in str.toCharArray()) {
        for (sym in "0123456789 +-:".toCharArray()) {
            if (symbol == sym) {
                break
            } else if (sym == ':') {
                return false
            }
        }
    }

    return true
}

fun removeNthChar(str: String, n: Int): String {
    return str.substring(0, n) + str.substring(n + 1)
}
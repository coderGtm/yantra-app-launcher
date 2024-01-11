package com.coderGtm.yantra.commands.timer

fun isValidLengthString(timeString: String): Boolean {
    if (timeString.toIntOrNull() != null) {
        val time = timeString.toInt()
        if (time in 1..86400) {
            return true
        }
    }
    return false
}
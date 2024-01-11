package com.coderGtm.yantra.commands.battery

fun isValidTimeString(ts: String): Boolean {
    val sp = ts.split(":")
    if (sp.size != 2) {
        return false
    }
    val hr = sp[0].toIntOrNull()
    val min = sp[1].toIntOrNull()

    if (hr == null || min == null) {
        return false
    }
    if (hr<0 || hr>23 || min<0 || min>59) {
        return false
    }
    return true
}
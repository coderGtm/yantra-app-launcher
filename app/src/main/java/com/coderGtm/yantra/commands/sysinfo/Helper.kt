package com.coderGtm.yantra.commands.sysinfo

import android.content.SharedPreferences
import com.coderGtm.yantra.Themes

fun getCPUSpeed(): String {
    try {
        val process = ProcessBuilder("/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().use { reader ->
            val cpuSpeed = reader.readLine()
            if (!cpuSpeed.isNullOrBlank()) {
                val speedInKHz = cpuSpeed.trim().toInt()
                val speedInGHz = speedInKHz / 1000000.0
                return String.format("%.2f GHz", speedInGHz)
            }
        }
    } catch (e: Exception) {
        return "-- GHz"
    }
    return "-- GHz"
}
fun getCurrentThemeName(preferenceObject: SharedPreferences): String {
    val id = preferenceObject.getInt("theme", 0)
    if (id == -1) {
        return preferenceObject.getString("customThemeName", "Custom") ?: "Custom"
    }
    return Themes.entries[id].name
}
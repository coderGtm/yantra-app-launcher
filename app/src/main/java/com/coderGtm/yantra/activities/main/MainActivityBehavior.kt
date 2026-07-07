package com.coderGtm.yantra.activities.main

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object MainActivityBehavior {
    fun shouldHandleCommand(command: String?): Boolean = !command.isNullOrBlank()

    fun shouldHandleSwipeCommand(isPro: Boolean, command: String?): Boolean =
        isPro && shouldHandleCommand(command)

    fun buildBackupFileName(date: Date = Date()): String {
        val formattedDate = SimpleDateFormat("HHmm_dd_MM_yyyy", Locale.getDefault()).format(date)
        return "backup_$formattedDate.yantra"
    }
}

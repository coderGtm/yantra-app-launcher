package com.coderGtm.yantra

import android.graphics.Color
import com.coderGtm.yantra.models.Theme

enum class Themes(val theme: Theme) {
    DEFAULT(
        Theme(
            bgColor = Color.BLACK,
            commandColor = Color.WHITE,
            suggestionTextColor = Color.WHITE,
            buttonColor = Color.WHITE,
            resultTextColor = Color.WHITE,
            errorTextColor = Color.RED,
            successTextColor = Color.GREEN,
            warningTextColor = Color.YELLOW
        )
    ),
    // Add more theme types as needed
}
enum class AppSortMode(val value: Int) {
    A_TO_Z(0),
    RECENT(1),
    MOST_USED(2)
}

const val SHARED_PREFS_FILE_NAME = "yantraSP"
const val DEFAULT_TERMINAL_FONT_NAME = "Source Code Pro"

val NO_LOG_COMMANDS = listOf("sleep", "echo", "notify")
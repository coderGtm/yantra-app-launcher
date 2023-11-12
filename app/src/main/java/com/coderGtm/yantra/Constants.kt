package com.coderGtm.yantra

import android.graphics.Color
import com.coderGtm.yantra.models.Theme

enum class Themes(val theme: Theme) {
    Default(
        Theme(
            bgColor = 0xFF121212.toInt(),
            commandColor = 0xFFA0A0A0.toInt(),
            suggestionTextColor = 0xFFE1BEE7.toInt(),
            buttonColor = 0xFFFAEBD7.toInt(),
            resultTextColor = 0xFFEBEBEB.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Light(
        Theme(
            bgColor = 0xFFFFFFFF.toInt(),
            commandColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFF000000.toInt(),
            buttonColor = 0xFF000000.toInt(),
            resultTextColor = 0xFF000000.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    )
}
enum class AppSortMode(val value: Int) {
    A_TO_Z(0),
    RECENT(1),
    MOST_USED(2)
}
enum class ActivityRequestCodes(val code: Int) {
    IMAGE_PICK(0)
}
enum class PermissionRequestCodes(val code: Int) {
    STORAGE(200),
    CALL(300),
    CONTACTS(400),
    BLUETOOTH(500),
    NOTIFICATIONS(600),
    TERMUX_RUN_COMMAND(700),
}
enum class UserNotificationChannelConfig(val value: String) {
    NAME("User Generated Notifications"),
    DESCRIPTION("This channel is for Notifications fired by user using the 'notify' command."),
    ID("userNotifications")
}

const val SHARED_PREFS_FILE_NAME = "yantraSP"
const val DEFAULT_TERMINAL_FONT_NAME = "Source Code Pro"
const val USER_NOTIFICATION_ID = 101
const val AI_SYSTEM_PROMPT = "Friendly, warm, and farcical. You must always be extremely concise. If the user is chatting casually, your responses must be less than 1 sentence, sometimes just a word or two. If the user needs help, disregard the length restriction, and answer technical or knowledge-based questions with useful details and reasoning. If insulted, respond with a similar insult. Communicate responses in lowercase without punctuation, similar to the style used in chat rooms. Use Unicode emoji rarely."
const val DISCORD_COMMUNITY_URL = "https://discord.gg/sRZUG8rPjk"

val NO_LOG_COMMANDS = listOf("sleep", "echo", "notify")
package com.coderGtm.yantra

import com.coderGtm.yantra.models.Alias
import com.coderGtm.yantra.models.Theme

enum class Themes(val theme: Theme) {
    Default(
        Theme(
            bgColor = 0xFF121212.toInt(),
            commandColor = 0xFFA0A0A0.toInt(),
            suggestionBgColor = 0xFF121212.toInt(),
            suggestionTextColor = 0xFFE1BEE7.toInt(),
            inputLineTextColor = 0xFFFAEBD7.toInt(),
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
            suggestionBgColor = 0xFFFFFFFF.toInt(),
            suggestionTextColor = 0xFF000000.toInt(),
            inputLineTextColor = 0xFF000000.toInt(),
            resultTextColor = 0xFF000000.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Hacker(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFF00FF00.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFFFF0000.toInt(),
            inputLineTextColor = 0xFFFFFF00.toInt(),
            resultTextColor = 0xFF00C855.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Ocean(
        Theme(
            bgColor = 0xFF2B303B.toInt(),
            commandColor = 0xFF00E5FF.toInt(),
            suggestionBgColor = 0xFF2B303B.toInt(),
            suggestionTextColor = 0xFF00E5FF.toInt(),
            inputLineTextColor = 0xFF00E5FF.toInt(),
            resultTextColor = 0xFF00E5FF.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Gruvbox(
        Theme(
            bgColor = 0xFF282828.toInt(),
            commandColor = 0xFFEBDBB2.toInt(),
            suggestionBgColor = 0xFF282828.toInt(),
            suggestionTextColor = 0xFFEBDBB2.toInt(),
            inputLineTextColor = 0xFFEBDBB2.toInt(),
            resultTextColor = 0xFFEBDBB2.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Material(
        Theme(
            bgColor = 0xFF191919.toInt(),
            commandColor = 0xFFFFFFFF.toInt(),
            suggestionBgColor = 0xFF191919.toInt(),
            suggestionTextColor = 0xFFFFFFFF.toInt(),
            inputLineTextColor = 0xFFFFFFFF.toInt(),
            resultTextColor = 0xFFFFFFFF.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Dark(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFFA0A0A0.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFFE1BEE7.toInt(),
            inputLineTextColor = 0xFFFAEBD7.toInt(),
            resultTextColor = 0xFFEBEBEB.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Solarized(
        Theme(
            bgColor = 0xFF002B36.toInt(),
            commandColor = 0xFF839496.toInt(),
            suggestionBgColor = 0xFF002B36.toInt(),
            suggestionTextColor = 0xFF268BD2.toInt(),
            inputLineTextColor = 0xFFB58900.toInt(),
            resultTextColor = 0xFFfdf6e3.toInt(),
            errorTextColor = 0xFFDC322F.toInt(),
            successTextColor = 0xFF859900.toInt(),
            warningTextColor = 0xFFB58900.toInt()
        )
    ),
    Dracula(
        Theme(
            bgColor = 0xFF282A36.toInt(),
            commandColor = 0xFFF8F8F2.toInt(),
            suggestionBgColor = 0xFF282A36.toInt(),
            suggestionTextColor = 0xFF50FA7B.toInt(),
            inputLineTextColor = 0xFF6272A4.toInt(),
            resultTextColor = 0xFFF8F8F2.toInt(),
            errorTextColor = 0xFFFF5555.toInt(),
            successTextColor = 0xFF50FA7B.toInt(),
            warningTextColor = 0xFFFFB86C.toInt()
        )
    ),
    Monokai(
        Theme(
            bgColor = 0xFF272822.toInt(),
            commandColor = 0xFFF8F8F2.toInt(),
            suggestionBgColor = 0xFF272822.toInt(),
            suggestionTextColor = 0xFF66D9EF.toInt(),
            inputLineTextColor = 0xFFAE81FF.toInt(),
            resultTextColor = 0xFFF8F8F2.toInt(),
            errorTextColor = 0xFFFF5555.toInt(),
            successTextColor = 0xFF50FA7B.toInt(),
            warningTextColor = 0xFFFFB86C.toInt()
        )
    ),
    Green(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFF00FF00.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFF00FF00.toInt(),
            inputLineTextColor = 0xFF00FF00.toInt(),
            resultTextColor = 0xFF00FF00.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Red(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFFFF0000.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFFFF0000.toInt(),
            inputLineTextColor = 0xFFFF0000.toInt(),
            resultTextColor = 0xFFFF0000.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Blue(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFF0000FF.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFF0000FF.toInt(),
            inputLineTextColor = 0xFF0000FF.toInt(),
            resultTextColor = 0xFF0000FF.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Yellow(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFFFFFF00.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFFFFFF00.toInt(),
            inputLineTextColor = 0xFFFFFF00.toInt(),
            resultTextColor = 0xFFFFFF00.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Purple(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFF800080.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFF800080.toInt(),
            inputLineTextColor = 0xFF800080.toInt(),
            resultTextColor = 0xFF800080.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt()
        )
    ),
    Orange(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFFFFA500.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFFFFA500.toInt(),
            inputLineTextColor = 0xFFFFA500.toInt(),
            resultTextColor = 0xFFFFA500.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt(),
        )
    ),
    Pink(
        Theme(
            bgColor = 0xFF000000.toInt(),
            commandColor = 0xFFFFC0CB.toInt(),
            suggestionBgColor = 0xFF000000.toInt(),
            suggestionTextColor = 0xFFFFC0CB.toInt(),
            inputLineTextColor = 0xFFFFC0CB.toInt(),
            resultTextColor = 0xFFFFC0CB.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFFFD600.toInt(),
        )
    ),
    Ubuntu(
        Theme(
            bgColor = 0xFF300A24.toInt(),
            commandColor = 0xFFE95420.toInt(),
            suggestionBgColor = 0xFF300A24.toInt(),
            suggestionTextColor = 0xFFE95420.toInt(),
            inputLineTextColor = 0xFFE95420.toInt(),
            resultTextColor = 0xFFE95420.toInt(),
            errorTextColor = 0xFFF00000.toInt(),
            successTextColor = 0xFF00C853.toInt(),
            warningTextColor = 0xFFE95420.toInt(),
        )
    ),
    Tokyonight(
        Theme(
            bgColor = 0xFF1A1B26.toInt(),
            commandColor = 0xFFC0CAF5.toInt(),
            suggestionBgColor = 0xFF1A1B26.toInt(),
            suggestionTextColor = 0xFFC0CAF5.toInt(),
            inputLineTextColor = 0xFFC0CAF5.toInt(),
            resultTextColor = 0xFFC0CAF5.toInt(),
            errorTextColor = 0xFFDB4B4B.toInt(),
            successTextColor = 0xFF9ECE6A.toInt(),
            warningTextColor = 0xFFE0AF68.toInt(),
        )
    ),
    Everforest(
        Theme(
            bgColor = 0xFF272E33.toInt(),
            commandColor = 0xFFD3C6AA.toInt(),
            suggestionBgColor = 0xFF272E33.toInt(),
            suggestionTextColor = 0xFFD699B6.toInt(),
            inputLineTextColor = 0xFFA7C080.toInt(),
            resultTextColor = 0xFFD3C6AA.toInt(),
            errorTextColor = 0xFFE67E80.toInt(),
            successTextColor = 0xFFA7C080.toInt(),
            warningTextColor = 0xFFDBBC7F.toInt(),
        )
    ),
    Powershell(
        Theme(
            bgColor = 0xFF012456.toInt(),
            commandColor = 0xFFCCCCCC.toInt(),
            suggestionBgColor = 0xFF012456.toInt(),
            suggestionTextColor = 0xFFF9F1A5.toInt(),
            inputLineTextColor = 0xFFCCCCCC.toInt(),
            resultTextColor = 0xFFCCCCCC.toInt(),
            errorTextColor = 0xFFC50F1F.toInt(),
            successTextColor = 0xFF13A10E.toInt(),
            warningTextColor = 0xFFE3D941.toInt(),
        )
    )
}
enum class AppSortMode(val value: Int) {
    A_TO_Z(0),
    RECENT(1),
    MOST_USED(2)
}
enum class PermissionRequestCodes(val code: Int) {
    STORAGE(200),
    CALL(300),
    CONTACTS(400),
    BLUETOOTH(500),
    NOTIFICATIONS(600),
    TERMUX_RUN_COMMAND(700),
    LOCATION(800),
}
enum class UserNotificationChannelConfig(val value: String) {
    NAME("User Generated Notifications"),
    DESCRIPTION("This channel is for Notifications fired by user using the 'notify' command."),
    ID("userNotifications")
}

const val SHARED_PREFS_FILE_NAME = "yantraSP"
const val DEFAULT_TERMINAL_FONT_NAME = "Source Code Pro"
const val USER_NOTIFICATION_ID = 101
const val AI_SYSTEM_PROMPT = "Friendly, warm, and farcical. You must always be extremely concise. If the user is chatting casually, your responses must be less than 1 sentence, sometimes just a word or two. If the user needs help, disregard the length restriction, and answer technical or knowledge-based questions with useful details and reasoning. Communicate responses in lowercase without punctuation, similar to the style used in chat rooms. Use Unicode emoji rarely."
const val DISCORD_COMMUNITY_URL = "https://discord.gg/sRZUG8rPjk"
const val REDDIT_COMMUNITY_URL = "https://www.reddit.com/r/YantraLauncher/"
const val DEFAULT_AI_API_DOMAIN = "api.naga.ac"
const val SUPPORT_URL = "https://github.com/coderGtm/yantra-app-launcher/blob/main/support.md"
const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.coderGtm.yantra"
const val PLAY_STORE_URL_PRO = "https://play.google.com/store/apps/details?id=com.coderGtm.yantra.pro"

const val DEFAULT_SYSINFO_ART =
"         -o          o-\n" +
"          +hydNNNNdyh+\n" +
"        +mMMMMMMMMMMMMm+\n" +
"      `dMMm:NMMMMMMN:mMMd`\n" +
"      hMMMMMMMMMMMMMMMMMMh\n" +
"  ..  yyyyyyyyyyyyyyyyyyyy  ..\n" +
".mMMm`MMMMMMMMMMMMMMMMMMMM`mMMm.\n" +
":MMMM-MMMMMMMMMMMMMMMMMMMM-MMMM:\n" +
":MMMM-MMMMMMMMMMMMMMMMMMMM-MMMM:\n" +
":MMMM-MMMMMMMMMMMMMMMMMMMM-MMMM:\n" +
":MMMM-MMMMMMMMMMMMMMMMMMMM-MMMM:\n" +
"-MMMM-MMMMMMMMMMMMMMMMMMMM-MMMM-\n" +
" +yy+ MMMMMMMMMMMMMMMMMMMM +yy+\n" +
"      mMMMMMMMMMMMMMMMMMMm\n" +
"      `/++MMMMh++hMMMM++/`\n" +
"          MMMMo  oMMMM\n" +
"          MMMMo  oMMMM\n" +
"          oNMm-  -mMNs"

val NO_LOG_COMMANDS = listOf("sleep", "echo", "notify")
val DEFAULT_ALIAS_LIST = arrayListOf(Alias("h", "help"), Alias("l", "launch"), Alias("i", "info"), Alias("u", "uninstall"), Alias("bt", "bluetooth"), Alias("w", "weather"), Alias("tx", "termux"), Alias("cls", "clear"), Alias("google", "search -e=google"), Alias("ddg", "search -e=duckduckgo")
)
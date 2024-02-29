package com.coderGtm.yantra.commands.translate

import android.graphics.Typeface
import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.coderGtm.yantra.R
import org.json.JSONArray
import org.json.JSONException

fun handleResponse(response: JSONArray, command: Command) {
    try {
        val translationsArray = response.getJSONArray(0)
        println(translationsArray)

        var translatedText = ""

        for (i in 0..<translationsArray.length()) {
            translatedText += translationsArray.getJSONArray(i).getString(0)
        }

        command.output(
            translatedText,
            command.terminal.theme.resultTextColor,
            Typeface.ITALIC,
            markdown = true
        )
    } catch (e: JSONException) {
        command.output(
            command.terminal.activity.getString(R.string.error_please_try_again),
            command.terminal.theme.errorTextColor
        )
    }
}

fun handleError(error: VolleyError, command: Command) {
    when (error) {
        is NoConnectionError -> {
            command.output(command.terminal.activity.getString(R.string.no_internet_connection), command.terminal.theme.errorTextColor)
        }

        else -> {
            command.output(command.terminal.activity.getString(R.string.an_error_occurred, error.networkResponse),command.terminal.theme.errorTextColor)
        }
    }
}

fun incorrectLanguage(language: String): Boolean {
    val correctLanguage = arrayOf(
        "az", "ay", "sq", "am", "chk", "en", "ar", "hy", "as", "af", "bm", "eu", "be", "bn", "my",
        "bg", "bs", "bho", "cy", "hu", "vi", "haw", "gl", "el", "ka", "gn", "gu", "da", "doi", "zu",
        "he", "ig", "yi", "ilo", "id", "ga", "is", "es", "it", "yo", "kk", "kn", "ca", "qu", "ky",
        "zh-TW", "zh-CN", "kok", "ko", "co", "xh", "ht", "ht", "hr", "mus", "cr", "ku", "sd", "km",
        "lo", "la", "lv", "ln", "lt", "lu", "lb", "mai", "mk", "mg", "ms", "ml", "dv", "mt", "mi",
        "mr", "mni", "lus", "mn", "de", "ne", "nl", "no", "or", "om", "pa", "fa", "pl", "pt", "ps",
        "rw", "ro", "hist", "ru", "sm", "sa", "ceb", "st", "sr", "sn", "si", "sd", "sk", "sl", "so",
        "sw", "su", "tl", "tg", "th", "ta", "tt", "te", "ti", "ts", "tr", "tk", "uz", "ug", "uk",
        "ur", "tl", "ph", "fi", "fr", "fy", "ha", "hi", "hmn", "hr", "cv", "ce", "cs", "sv", "sn",
        "gd", "ee", "eo", "et", "jv", "ja"
    )

    return language !in correctLanguage
}
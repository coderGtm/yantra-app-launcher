package com.coderGtm.yantra.commands.translate

import android.graphics.Typeface
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import org.json.JSONArray
import org.json.JSONException

fun handleResponse(response: JSONArray, command: Command) {
    try {
        val translationsArray = response.getJSONArray(0)
        println(translationsArray)
        val translatedText = translationsArray.getJSONArray(0).getString(0)

        command.output(
            translatedText,
            command.terminal.theme.resultTextColor,
            Typeface.ITALIC,
            markdown = true
        )
    } catch (e: JSONException) {
        e.printStackTrace()

        command.output(
            "Error, please try again.",
            command.terminal.theme.errorTextColor
        )
    }
}

fun handleError(error: VolleyError, command: Command) {
    when (error) {
        is NoConnectionError -> {
            command.output("No internet connection", command.terminal.theme.errorTextColor)
        }

        is TimeoutError -> {
            command.output("I lost my patience! Try again or try a request with a shorter expected output.", command.terminal.theme.errorTextColor)
        }

        else -> {
            command.output("An error occurred: ${error.networkResponse}",command.terminal.theme.errorTextColor)
        }
    }
}
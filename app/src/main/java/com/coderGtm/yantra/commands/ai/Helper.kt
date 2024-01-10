package com.coderGtm.yantra.commands.ai

import android.graphics.Typeface
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import org.json.JSONObject

fun handleResponse(response: JSONObject, command: Command) {
    val jsonResponse = response.toString()
    val jsonObject = JSONObject(jsonResponse)

    if (jsonObject.has("result")) {
        val resultObject = jsonObject.getJSONObject("result")
        if (resultObject.has("choices")) {
            val choicesArray = resultObject.getJSONArray("choices")
            if (choicesArray.length() > 0) {
                val firstChoice = choicesArray.getJSONObject(0)
                val replyContent = firstChoice.getString("text")

                command.output(replyContent, command.terminal.theme.resultTextColor, Typeface.ITALIC, markdown = true)
            } else {
                command.output("No 'choices' found in the response", command.terminal.theme.errorTextColor)
            }
        } else {
            command.output("No 'choices' found in the 'result' object", command.terminal.theme.errorTextColor)
        }
    } else {
        command.output("No 'result' object found in the response", command.terminal.theme.errorTextColor)
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

        is AuthFailureError -> {
            command.output("Authentication Failed. Make sure you used the correct API key in 'settings'", command.terminal.theme.errorTextColor)
        }

        else -> {
            command.output("An error occurred: ${error.networkResponse.statusCode}",command.terminal.theme.errorTextColor)
        }
    }
}
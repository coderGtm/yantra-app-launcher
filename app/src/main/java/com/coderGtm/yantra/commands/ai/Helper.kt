package com.coderGtm.yantra.commands.ai

import android.graphics.Typeface
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import org.json.JSONArray
import org.json.JSONObject

fun getRequestBody(systemPrompt: String, message: String): JSONObject {
    val requestBody = JSONObject().apply {
        put("model", "gpt-3.5-turbo")
        put("messages", JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", message)
            })
        })
    }
    return requestBody
}

fun handleResponse(response: JSONObject, command: Command) {
    val jsonResponse = response.toString()
    val jsonObject = JSONObject(jsonResponse)

    if (jsonObject.has("choices")) {
        // Extract the reply content
        val choicesArray = jsonObject.getJSONArray("choices")
        if (choicesArray.length() > 0) {
            val firstChoice = choicesArray.getJSONObject(0)
            val replyContent = firstChoice.getJSONObject("message").getString("content").replace("\\n","\n")

            command.output(replyContent, command.terminal.theme.resultTextColor, Typeface.ITALIC)
        } else {
            command.output("No reply found in the response", command.terminal.theme.errorTextColor)
        }
    }
    else {
        command.output("The server did not send a chat reply! Try again.", command.terminal.theme.errorTextColor)
    }
}

fun handleError(error: VolleyError, command: Command) {
    when (error) {
        is NoConnectionError -> {
            command.output("No internet connection", command.terminal.theme.errorTextColor)
        }

        is TimeoutError -> {
            command.output("Request Timed out. Try again or try a request with a shorter expected output.", command.terminal.theme.errorTextColor)
        }

        is AuthFailureError -> {
            command.output("Authentication Failed. Make sure you used the correct API key in 'settings'", command.terminal.theme.errorTextColor)
        }

        else -> {
            command.output("An error occurred: $error",command.terminal.theme.errorTextColor)
        }
    }
}
package com.coderGtm.yantra.commands.ai

import android.graphics.Typeface
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.coderGtm.yantra.R
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
            val replyContent = firstChoice.getJSONObject("message").getString("content")

            command.output(replyContent, command.terminal.theme.resultTextColor, Typeface.ITALIC, markdown = true)
        } else {
            command.output(command.terminal.activity.getString(R.string.no_reply_in_response), command.terminal.theme.errorTextColor)
        }
    }
    else {
        command.output(command.terminal.activity.getString(R.string.no_reply_from_server), command.terminal.theme.errorTextColor)
    }
}

fun handleError(error: VolleyError, command: Command) {
    when (error) {
        is NoConnectionError -> {
            command.output(command.terminal.activity.getString(R.string.no_internet_connection), command.terminal.theme.errorTextColor)
        }

        is TimeoutError -> {
            command.output(command.terminal.activity.getString(R.string.timeout_error), command.terminal.theme.errorTextColor)
        }

        is AuthFailureError -> {
            command.output(command.terminal.activity.getString(R.string.ai_auth_failed), command.terminal.theme.errorTextColor)
        }

        else -> {
            command.output(command.terminal.activity.getString(R.string.ai_error, error.networkResponse.statusCode),command.terminal.theme.errorTextColor)
        }
    }
}
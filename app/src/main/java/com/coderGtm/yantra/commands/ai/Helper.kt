package com.coderGtm.yantra.commands.ai

import android.graphics.Typeface
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.coderGtm.yantra.R
import com.coderGtm.yantra.terminal.Terminal
import org.json.JSONArray
import org.json.JSONObject

fun getRequestBody(systemPrompt: String, message: String, terminal: Terminal): JSONObject {
    val messageHistory = getMessageHistory(terminal)
    val requestBody = JSONObject().apply {
        put("model", "gpt-3.5-turbo")
        put("messages", JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            messageHistory.forEach {
                put(it)
            }
            put(JSONObject().apply {
                put("role", "user")
                put("content", message)
            })
        })
    }
    
    return requestBody
}

fun handleResponse(response: JSONObject, command: Command, requestBody: JSONObject) {
    val jsonResponse = response.toString()
    val jsonObject = JSONObject(jsonResponse)

    if (jsonObject.has("choices")) {
        // Extract the reply content
        val choicesArray = jsonObject.getJSONArray("choices")
        if (choicesArray.length() > 0) {
            val firstChoice = choicesArray.getJSONObject(0)
            val replyContent = firstChoice.getJSONObject("message").getString("content")

            command.output(replyContent, command.terminal.theme.resultTextColor, Typeface.ITALIC, markdown = true)
            addReplyToMessageHistory(command.terminal, replyContent, requestBody)
        } else {
            command.output(command.terminal.activity.getString(R.string.no_reply_in_response), command.terminal.theme.errorTextColor)
        }
        // print tokens
        if (jsonObject.has("usage")) {
            val usage = jsonObject.getJSONObject("usage")
            val tokens = usage.get("total_tokens")
            command.output(command.terminal.activity.getString(R.string.total_tokens_used, tokens), command.terminal.theme.warningTextColor)
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

private fun getMessageHistory(terminal: Terminal): ArrayList<JSONObject> {
    val messageHistory = ArrayList<JSONObject>()
    val messages = terminal.preferenceObject.getString("aiMessageHistory", "[]") ?: "[]"
    val jsonArray = JSONArray(messages)
    for (i in 0 until jsonArray.length()) {
        messageHistory.add(jsonArray.getJSONObject(i))
    }
    return messageHistory
}

private fun addReplyToMessageHistory(terminal: Terminal, reply: String, requestBody: JSONObject) {
    // add the reply to request body
    // request body has the entire conversation history
    val messages = requestBody.getJSONArray("messages")

    // remove the system prompt from the history
    messages.remove(0)

    messages.put(JSONObject().apply {
        put("role", "assistant")
        put("content", reply)
    })

    // save the updated message history
    terminal.preferenceObject.edit().putString("aiMessageHistory", messages.toString()).apply()
}
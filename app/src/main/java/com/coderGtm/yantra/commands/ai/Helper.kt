package com.coderGtm.yantra.commands.ai

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.terminal.Terminal
import io.ktor.client.plugins.ResponseException
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import org.json.JSONArray
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import androidx.core.content.edit

fun getRequestBody(systemPrompt: String, message: String, streamResponse: Boolean, terminal: Terminal): JSONObject {
    val messageHistory = getMessageHistory(terminal)
    val requestBody = JSONObject().apply {
        put("model", terminal.preferenceObject.getString("aiModel", "chatgpt-4o-latest"))
        put("stream", streamResponse)
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

fun handleResponse(response: JSONObject, command: Command, requestBody: JSONObject, outputId: String? = null) {
    if (!response.has("choices")) {
        outputToTerminal(
            command = command,
            text = command.terminal.activity.getString(R.string.no_reply_from_server),
            color = command.terminal.theme.errorTextColor,
            outputId = outputId
        )
        return
    }

    val choicesArray = response.getJSONArray("choices")
    if (choicesArray.length() == 0) {
        outputToTerminal(
            command = command,
            text = command.terminal.activity.getString(R.string.no_reply_in_response),
            color = command.terminal.theme.errorTextColor,
            outputId = outputId
        )
        return
    }

    val replyContent = choicesArray.getJSONObject(0)
        .getJSONObject("message")
        .getString("content")

    outputToTerminal(
        command = command,
        text = replyContent,
        color = command.terminal.theme.resultTextColor,
        style = Typeface.ITALIC,
        markdown = true,
        outputId = outputId
    )
    addReplyToMessageHistory(command.terminal, replyContent, requestBody)

    if (outputId == null && response.has("usage")) {
        val tokens = response.getJSONObject("usage").get("total_tokens")
        command.output(command.terminal.activity.getString(R.string.total_tokens_used, tokens), command.terminal.theme.warningTextColor)
    }
}

fun handleKtorError(error: Exception, command: Command, outputId: String? = null) {
    when (error) {
        is ConnectException, is UnknownHostException -> {
            outputToTerminal(
                command = command,
                text = command.terminal.activity.getString(R.string.no_internet_connection),
                color = command.terminal.theme.errorTextColor,
                outputId = outputId
            )
        }

        is SocketTimeoutException -> {
            outputToTerminal(
                command = command,
                text = command.terminal.activity.getString(R.string.timeout_error),
                color = command.terminal.theme.errorTextColor,
                outputId = outputId
            )
        }

        is ResponseException -> {
            val statusCode = error.response.status.value
            val message = if (statusCode == 401) {
                command.terminal.activity.getString(R.string.ai_auth_failed)
            } else {
                command.terminal.activity.getString(R.string.ai_error, statusCode)
            }
            outputToTerminal(
                command = command,
                text = message,
                color = command.terminal.theme.errorTextColor,
                outputId = outputId
            )
        }

        else -> {
            outputToTerminal(
                command = command,
                text = command.terminal.activity.getString(R.string.an_error_occurred_please_try_again),
                color = command.terminal.theme.errorTextColor,
                outputId = outputId
            )
        }
    }
}

suspend fun handleStreamingResponse(
    responseChannel: ByteReadChannel,
    command: Command,
    requestBody: JSONObject,
    outputId: String
) {
    val streamedResponse = StringBuilder()
    var completedResponseSnapshot: JSONObject? = null
    var totalTokens: Any? = null
    var currentEvent: String? = null

    while (!responseChannel.isClosedForRead) {
        val line = responseChannel.readUTF8Line() ?: break
        if (line.isBlank()) {
            currentEvent = null
            continue
        }

        if (line.startsWith("event:")) {
            currentEvent = line.removePrefix("event:").trim()
            continue
        }

        if (!line.startsWith("data:")) {
            continue
        }

        val payload = line.removePrefix("data:").trim()
        if (payload.isEmpty()) continue
        if (payload == "[DONE]") {
            finalizeStreamingResponse(
                command = command,
                requestBody = requestBody,
                outputId = outputId,
                streamedResponse = streamedResponse,
                completedResponseSnapshot = completedResponseSnapshot,
                totalTokens = totalTokens
            )
            return
        }

        val jsonObject = JSONObject(payload)
        if (jsonObject.has("usage")) {
            totalTokens = jsonObject.getJSONObject("usage").opt("total_tokens")
        }
        if (currentEvent == "response.completed" || isCompletedResponseSnapshot(jsonObject)) {
            completedResponseSnapshot = jsonObject
            if (totalTokens == null && jsonObject.has("usage")) {
                totalTokens = jsonObject.getJSONObject("usage").opt("total_tokens")
            }
            continue
        }
        val choicesArray = jsonObject.optJSONArray("choices") ?: continue
        if (choicesArray.length() == 0) continue

        val delta = choicesArray.getJSONObject(0).optJSONObject("delta") ?: continue
        val content = delta.optString("content")
        if (content.isEmpty()) continue

        streamedResponse.append(content)
        outputToTerminal(
            command = command,
            text = streamedResponse.toString(),
            color = command.terminal.theme.resultTextColor,
            style = Typeface.ITALIC,
            outputId = outputId
        )
    }

    finalizeStreamingResponse(
        command = command,
        requestBody = requestBody,
        outputId = outputId,
        streamedResponse = streamedResponse,
        completedResponseSnapshot = completedResponseSnapshot,
        totalTokens = totalTokens
    )
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
    terminal.preferenceObject.edit { putString("aiMessageHistory", messages.toString()) }
}

private fun outputToTerminal(
    command: Command,
    text: String,
    color: Int,
    style: Int? = null,
    markdown: Boolean = false,
    outputId: String? = null
) {
    if (outputId == null) {
        command.output(text, color, style, markdown)
        return
    }
    command.terminal.updateOutputItem(outputId, text, color, style, markdown)
}

private fun finalizeStreamingResponse(
    command: Command,
    requestBody: JSONObject,
    outputId: String,
    streamedResponse: StringBuilder,
    completedResponseSnapshot: JSONObject?,
    totalTokens: Any?
) {
    if (streamedResponse.isNotEmpty()) {
        val replyContent = streamedResponse.toString()
        outputToTerminal(
            command = command,
            text = replyContent,
            color = command.terminal.theme.resultTextColor,
            style = Typeface.ITALIC,
            markdown = true,
            outputId = outputId
        )
        addReplyToMessageHistory(command.terminal, replyContent, requestBody)
        if (totalTokens != null) {
            command.output(
                command.terminal.activity.getString(R.string.total_tokens_used, totalTokens),
                command.terminal.theme.warningTextColor
            )
        }
        return
    }

    if (completedResponseSnapshot != null) {
        val completedText = extractCompletedResponseText(completedResponseSnapshot)
        if (!completedText.isNullOrEmpty()) {
            outputToTerminal(
                command = command,
                text = completedText,
                color = command.terminal.theme.resultTextColor,
                style = Typeface.ITALIC,
                markdown = true,
                outputId = outputId
            )
            addReplyToMessageHistory(command.terminal, completedText, requestBody)
            if (totalTokens != null) {
                command.output(
                    command.terminal.activity.getString(R.string.total_tokens_used, totalTokens),
                    command.terminal.theme.warningTextColor
                )
            }
            return
        }
        handleResponse(completedResponseSnapshot, command, requestBody, outputId)
        return
    }

    if (totalTokens != null) {
        command.output(
            command.terminal.activity.getString(R.string.total_tokens_used, totalTokens),
            command.terminal.theme.warningTextColor
        )
        return
    }

    outputToTerminal(
        command = command,
        text = command.terminal.activity.getString(R.string.no_reply_from_server),
        color = command.terminal.theme.errorTextColor,
        outputId = outputId
    )
}

private fun extractCompletedResponseText(response: JSONObject?): String? {
    if (response == null) return null

    val directOutputText = response.optString("output_text")
    if (directOutputText.isNotEmpty()) {
        return directOutputText
    }

    val output = response.optJSONArray("output") ?: return null
    val textParts = mutableListOf<String>()

    for (i in 0 until output.length()) {
        val item = output.optJSONObject(i) ?: continue
        val contentArray = item.optJSONArray("content")
        if (contentArray != null) {
            for (j in 0 until contentArray.length()) {
                val content = contentArray.optJSONObject(j) ?: continue
                val text = content.optString("text")
                if (text.isNotEmpty()) {
                    textParts += text
                }
            }
        }

        val directText = item.optString("text")
        if (directText.isNotEmpty()) {
            textParts += directText
        }
    }

    return textParts.joinToString("").ifEmpty { null }
}

private fun isCompletedResponseSnapshot(jsonObject: JSONObject): Boolean {
    return jsonObject.optString("object") == "response" && jsonObject.optString("status") == "completed"
}
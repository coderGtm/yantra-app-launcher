package com.coderGtm.yantra.commands.ai

import android.graphics.Typeface
import com.coderGtm.yantra.AI_SYSTEM_PROMPT
import com.coderGtm.yantra.DEFAULT_AI_API_DOMAIN
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.network.HttpClientProvider
import com.coderGtm.yantra.terminal.Terminal
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "ai",
        helpTitle = terminal.activity.getString(R.string.cmd_ai_title),
        description = terminal.activity.getString(R.string.cmd_ai_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        val message = command.removePrefix(args[0])
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.ai_msg_missing), terminal.theme.errorTextColor)
            return
        }
        if (message.trim() == "reset") {
            terminal.preferenceObject.edit().remove("aiMessageHistory").apply()
            output(terminal.activity.getString(R.string.ai_reset), terminal.theme.successTextColor)
            return
        }
        val apiDomain = terminal.preferenceObject.getString("aiApiDomain", DEFAULT_AI_API_DOMAIN) ?: DEFAULT_AI_API_DOMAIN
        val url = "https://$apiDomain/v1/chat/completions"
        val apiKey = terminal.preferenceObject.getString("aiApiKey", "") ?: ""
        val systemPrompt = terminal.preferenceObject.getString("aiSystemPrompt", AI_SYSTEM_PROMPT) ?: AI_SYSTEM_PROMPT
        val shouldStream = terminal.preferenceObject.getBoolean("streamAiResponse", true)
        val requestBody = getRequestBody(systemPrompt, message, shouldStream, terminal)

        if (apiKey == "") {
            output(terminal.activity.getString(R.string.no_ai_api_key_found), terminal.theme.errorTextColor, Typeface.BOLD_ITALIC)
            return
        }

        val streamingOutputId = if (shouldStream) {
            output(
                text = terminal.activity.getString(R.string.communicating_with_ai),
                state = terminal.theme.resultTextColor,
                style = Typeface.BOLD_ITALIC
            )
        } else {
            null
        }

        if (!shouldStream) {
            output(terminal.activity.getString(R.string.communicating_with_ai), terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (shouldStream) {
                    withContext(Dispatchers.IO) {
                        HttpClientProvider.client.preparePost(url) {
                            header(HttpHeaders.Authorization, "Bearer $apiKey")
                            contentType(ContentType.Application.Json)
                            setBody(requestBody.toString())
                        }.execute { response ->
                            handleStreamingResponse(
                                responseChannel = response.bodyAsChannel(),
                                command = this@Command,
                                requestBody = requestBody,
                                outputId = streamingOutputId!!
                            )
                        }
                    }
                } else {
                    val responseText = withContext(Dispatchers.IO) {
                        HttpClientProvider.client.preparePost(url) {
                            header(HttpHeaders.Authorization, "Bearer $apiKey")
                            contentType(ContentType.Application.Json)
                            setBody(requestBody.toString())
                        }.execute { response ->
                            response.bodyAsText()
                        }
                    }
                    handleResponse(JSONObject(responseText), this@Command, requestBody)
                }
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                handleKtorError(e, this@Command, streamingOutputId)
            }
        }
    }
}
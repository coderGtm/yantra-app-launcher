package com.coderGtm.yantra.commands.ai

import android.graphics.Typeface
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.AI_SYSTEM_PROMPT
import com.coderGtm.yantra.DEFAULT_AI_API_DOMAIN
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

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
        val apiDomain = terminal.preferenceObject.getString("aiApiDomain", DEFAULT_AI_API_DOMAIN) ?: DEFAULT_AI_API_DOMAIN
        val url = "https://$apiDomain/v1/chat/completions"
        val apiKey = terminal.preferenceObject.getString("aiApiKey", "") ?: ""
        val systemPrompt = terminal.preferenceObject.getString("aiSystemPrompt", AI_SYSTEM_PROMPT) ?: AI_SYSTEM_PROMPT
        val requestBody = getRequestBody(systemPrompt, message)

        if (apiKey == "") {
            output(terminal.activity.getString(R.string.no_ai_api_key_found), terminal.theme.errorTextColor, Typeface.BOLD_ITALIC)
            return
        }

        // Create a Volley request
        val request = object: JsonObjectRequest(
            Method.POST,
            url,
            requestBody,
            { response ->
                handleResponse(response, this@Command)
            },
            { error ->
                handleError(error, this@Command)
            }
        )

        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $apiKey"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        request.retryPolicy = DefaultRetryPolicy(1000 * 60 * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        // Add the request to the Volley queue for execution
        val requestQueue = Volley.newRequestQueue(terminal.activity)
        requestQueue.add(request)
        output(terminal.activity.getString(R.string.communicating_with_ai), terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)
    }
}
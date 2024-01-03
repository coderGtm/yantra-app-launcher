package com.coderGtm.yantra.commands.dict

import android.graphics.Typeface
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "dict",
        helpTitle = "dict [word]",
        description = "Search for the meaning of a word in an online dictionary (freeDictionaryAPI)"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size <= 1) {
            output("Please specify the word to search for.", terminal.theme.errorTextColor)
            return
        }
        val word = command.removePrefix(args[0]).trim()
        val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"

        // Create a Volley request
        val request = object: JsonArrayRequest (
            Method.GET,
            url,
            null,
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
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        request.retryPolicy = DefaultRetryPolicy(1000 * 60 * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        val requestQueue = Volley.newRequestQueue(terminal.activity)
        requestQueue.add(request)
        output("Looking up '$word' in the dictionary...", terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)
    }
}
package com.coderGtm.yantra.commands.translate

import android.graphics.Typeface
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "translate",
        helpTitle = "translate [-language] [text]",
        description = "Translator based on Google Translate."
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size < 3) {
            output("Please, specify language and text.", terminal.theme.errorTextColor)
            return
        }

        val language = args[1]
        val message = command.removePrefix(args[0] + " " + args[1])

        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" +
                language + "&dt=t&q=" + java.net.URLEncoder.encode(message, "UTF-8")

        val request = object : JsonArrayRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                handleResponse(response, this@Command)
            },
            Response.ErrorListener { error ->
                // Handling error
                handleError(error, this@Command)
                println(error)
            }
        )
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Connection"] = "keep-alive"
                headers["Accept"] = "*/*"
                headers["User-Agent"] = "PostmanRuntime/7.36.1"
                return headers
            }
        }

        request.retryPolicy = DefaultRetryPolicy(1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

// Add the request to the Volley queue for execution
        val requestQueue = Volley.newRequestQueue(terminal.activity)
        requestQueue.add(request)
        output("Translating...", terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)

    }
}
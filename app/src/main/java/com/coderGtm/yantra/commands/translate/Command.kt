package com.coderGtm.yantra.commands.translate

import android.graphics.Typeface
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "translate",
        helpTitle = terminal.activity.getString(R.string.cmd_translate_title),
        description = terminal.activity.getString(R.string.cmd_translate_help)
    )

    override fun execute(command: String) {
        // Split the command into individual arguments.
        val args = command.split(" ")

        // Check for insufficient arguments.
        if (args.size < 3) {
            output(terminal.activity.getString(R.string.translate_give_lang_code), terminal.theme.errorTextColor)
            return
        }

        // Extract the target language and message from the command.
        val language = args[1].removePrefix("-")
        val message = command.substringAfter(language).trim()

        // Check for incorrect language.
        if (incorrectLanguage(language)) {
            output(terminal.activity.getString(R.string.language_code_not_found), terminal.theme.errorTextColor)
            return
        }

        // Construct the URL for translation.
        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" +
                language + "&dt=t&q=" + java.net.URLEncoder.encode(message, "UTF-8")

        // Create a JSON request to the translation API.
        val request = object : JsonArrayRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                // Handle a successful response.
                handleResponse(response, this@Command)
            },
            Response.ErrorListener { error ->
                // Handle an error that occurs during the request.
                handleError(error, this@Command)
            }
        )
        {
            // Set headers for the request.
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Yantra Launcher"

                return headers
            }
        }

        // Configure the request retry policy.
        request.retryPolicy = DefaultRetryPolicy(1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        // Create a request queue and add the translation request to it.
        val requestQueue = Volley.newRequestQueue(terminal.activity)
        requestQueue.add(request)

        // Display a message indicating that translation is in progress.
        output(terminal.activity.getString(R.string.translating), terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)
    }
}
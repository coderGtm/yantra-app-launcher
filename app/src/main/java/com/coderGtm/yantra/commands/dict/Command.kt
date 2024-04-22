package com.coderGtm.yantra.commands.dict

import android.graphics.Typeface
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "dict",
        helpTitle = terminal.activity.getString(R.string.cmd_dict_title),
        description = terminal.activity.getString(R.string.cmd_dict_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size <= 1) {
            output(terminal.activity.getString(R.string.please_specify_word_to_search), terminal.theme.errorTextColor)
            return
        }

        val word: String
        val url: String
        val dictionary: String

        if (args[1].trim() == "-urban") {
            if (args.size <= 2) {
                output(terminal.activity.getString(R.string.please_specify_word_to_search), terminal.theme.errorTextColor)
                return
            }
            word = command.removePrefix(args[0]).trim().removePrefix(args[1]).trim()
            url = "https://api.urbandictionary.com/v0/define?term=$word"
            dictionary = "urban"
        }
        else {
            word = command.removePrefix(args[0]).trim()
            url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"
            dictionary = "freeDictionaryAPI"
        }



        // Create a Volley request
        if (dictionary == "freeDictionaryAPI") {
            val request = object: JsonArrayRequest (
                Method.GET,
                url,
                null,
                { response ->
                    handleFreeDictionaryResponse(response, this@Command)
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
            output(terminal.activity.getString(R.string.looking_up_in_the_dictionary, word), terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)
        }
        else {
            val request = object: JsonObjectRequest (
                Method.GET,
                url,
                null,
                { response ->
                    handleUrbanResponse(response, this@Command)
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
            output(terminal.activity.getString(R.string.looking_up_in_the_dictionary, word), terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)
        }

    }
}
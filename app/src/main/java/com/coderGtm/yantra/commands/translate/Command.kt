package com.coderGtm.yantra.commands.translate

import android.graphics.Typeface
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import org.json.JSONObject

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

        val provider = terminal.preferenceObject.getString("translationApiProvider", "google") ?: "google"
        val apiKey = terminal.preferenceObject.getString("translationApiProvider", "") ?: ""

        // Extract the target language and message from the command.
        val language = args[1].removePrefix("-")
        val message = command.substringAfter(language).trim()

        // Check for incorrect language.
        if (incorrectLanguage(language, provider) && provider == "google") {
            output(terminal.activity.getString(R.string.language_code_not_found), terminal.theme.errorTextColor)
            return
        }

        // Construct the URL for translation.
        val url = if (provider == "google") {
            "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" +
                    language + "&dt=t&q=" + java.net.URLEncoder.encode(message, "UTF-8")
        } else if (provider == "deepl") {
            "https://api.deepl.com/v2/translate"
        } else {
            ""
        }

        // Create a JSON request to the translation API.
        if (provider == "google") {
            val request = object : JsonArrayRequest(
                Method.GET,
                url,
                null,
                Response.Listener { response ->
                    // Handle a successful response.
                    handleGoogleResponse(response, this@Command)
                },
                Response.ErrorListener { error ->
                    // Handle an error that occurs during the request.
                    handleGoogleError(error, this@Command)
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
        }
        else if (provider == "deepl") {
            AndroidNetworking.post(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "DeepL-Auth-Key $apiKey")
                .addBodyParameter("text", message)
                .addBodyParameter("target_lang", language)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        handleDeepLResponse(response, this@Command)
                    }

                    override fun onError(error: com.androidnetworking.error.ANError) {
                        handleDeeplError(error, this@Command)
                    }
                })
        }



        // Display a message indicating that translation is in progress.
        output(terminal.activity.getString(R.string.translating), terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)
    }
}
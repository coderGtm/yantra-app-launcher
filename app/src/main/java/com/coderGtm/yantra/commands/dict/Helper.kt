package com.coderGtm.yantra.commands.dict

import android.graphics.Typeface
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import org.json.JSONArray
import org.json.JSONObject

fun handleResponse(response: JSONArray, command: Command) {
    val jResponse = response.getJSONObject(0)
    command.output("Word: ${jResponse.getString("word")}", command.terminal.theme.resultTextColor, Typeface.BOLD)
    command.output("==================")
    showPhonetics(jResponse, command)
    command.output("------------------")
    showMeanings(jResponse, command)
    command.output("==================")
}

fun showPhonetics(response: JSONObject, command: Command) {
    command.output("--> Phonetics", command.terminal.theme.resultTextColor, Typeface.BOLD)
    try {
        val phonetics = response.getJSONArray("phonetics")
        for (i in 0 until phonetics.length()) {
            val phonetic = phonetics.getJSONObject(i)
            if (phonetic.has("text")) {
                val text = phonetic.getString("text")
                command.output(text, command.terminal.theme.resultTextColor)
            }
        }
    } catch (e: Exception) {
        command.output("No phonetics found", command.terminal.theme.warningTextColor, Typeface.ITALIC)
    }
}

fun showMeanings(response: JSONObject, command: Command) {
    command.output("--> Meanings", command.terminal.theme.resultTextColor, Typeface.BOLD)
    try {
        val meanings = response.getJSONArray("meanings")
        for (i in 0 until meanings.length()) {
            val meaning = meanings.getJSONObject(i)
            val partOfSpeech = meaning.getString("partOfSpeech")
            command.output(partOfSpeech, command.terminal.theme.warningTextColor, Typeface.BOLD_ITALIC)
            val definitions = meaning.getJSONArray("definitions")
            for (j in 0 until definitions.length()) {
                val definition = definitions.getJSONObject(j)
                val definitionText = definition.getString("definition")
                command.output(definitionText, command.terminal.theme.resultTextColor)
                try {
                    val example = definition.getString("example")
                    command.output("\nExample: $example\n", command.terminal.theme.resultTextColor, Typeface.ITALIC)
                    command.output("------------------")
                } catch (e: Exception) {}
            }
        }
    } catch (e: Exception) {}
}

fun handleError(error: Exception, command: Command) {
    when (error) {
        is NoConnectionError -> {
            command.output("No internet connection! This command relies on an online Dictionary API, so an internet connection is required to access it.", command.terminal.theme.errorTextColor)
        }

        is TimeoutError -> {
            command.output("I lost my patience! Try again or try a request with a shorter expected output.", command.terminal.theme.errorTextColor)
        }

        else -> {
            command.output("Sorry pal, we couldn't find definitions for the word you were looking for.",command.terminal.theme.errorTextColor)
        }
    }
}
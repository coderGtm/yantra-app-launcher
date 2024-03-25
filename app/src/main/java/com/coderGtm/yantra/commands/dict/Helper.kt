package com.coderGtm.yantra.commands.dict

import android.graphics.Typeface
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.coderGtm.yantra.R
import org.json.JSONArray
import org.json.JSONObject

fun handleResponse(response: JSONArray, command: Command) {
    val jResponse = response.getJSONObject(0)
    command.output(command.terminal.activity.getString(R.string.dict_word, jResponse.getString("word")), command.terminal.theme.resultTextColor, Typeface.BOLD)
    command.output("==================")
    showPhonetics(jResponse, command)
    command.output("------------------")
    showMeanings(jResponse, command)
    command.output("==================")
}

fun showPhonetics(response: JSONObject, command: Command) {
    command.output(command.terminal.activity.getString(R.string.dict_phonetics), command.terminal.theme.resultTextColor, Typeface.BOLD)
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
        command.output(command.terminal.activity.getString(R.string.no_phonetics_found), command.terminal.theme.warningTextColor, Typeface.ITALIC)
    }
}

fun showMeanings(response: JSONObject, command: Command) {
    command.output(command.terminal.activity.getString(R.string.dict_meanings), command.terminal.theme.resultTextColor, Typeface.BOLD)
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
                    command.output(command.terminal.activity.getString(R.string.dict_example, example), command.terminal.theme.resultTextColor, Typeface.ITALIC)
                    command.output("------------------")
                } catch (e: Exception) {}
            }
        }
    } catch (e: Exception) {}
}

fun handleError(error: Exception, command: Command) {
    when (error) {
        is NoConnectionError -> {
            command.output(command.terminal.activity.getString(R.string.dict_no_internet), command.terminal.theme.errorTextColor)
        }

        is TimeoutError -> {
            command.output(command.terminal.activity.getString(R.string.timeout_error), command.terminal.theme.errorTextColor)
        }

        else -> {
            command.output(command.terminal.activity.getString(R.string.dict_word_not_found),command.terminal.theme.errorTextColor)
        }
    }
}
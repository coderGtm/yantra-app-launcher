package com.coderGtm.yantra.commands.bored

import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import org.json.JSONObject

fun handleResponse(response: String, command: Command) {
    val json = JSONObject(response)
    val activity = json.getString("activity")
    val type = json.getString("type")
    val participants = json.getString("participants")
    command.output("-------------------------")
    command.output("Random Activity", command.terminal.theme.successTextColor)
    command.output("=> $activity")
    command.output("=> Type: $type")
    command.output("=> Participants: $participants")
    command.output("-------------------------")
}

fun handleError(error: VolleyError, command: Command) {
    if (error is NoConnectionError) {
        command.output("No internet connection", command.terminal.theme.errorTextColor)
    }
    else {
        command.output("An error occurred.",command.terminal.theme.errorTextColor)
    }
}
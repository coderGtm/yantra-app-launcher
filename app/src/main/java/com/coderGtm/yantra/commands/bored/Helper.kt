package com.coderGtm.yantra.commands.bored

import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.coderGtm.yantra.R
import org.json.JSONObject

fun handleResponse(response: String, command: Command) {
    val json = JSONObject(response)
    val activity = json.getString("activity")
    val type = json.getString("type")
    val participants = json.getString("participants")
    command.output("-------------------------")
    command.output(command.terminal.activity.getString(R.string.random_activity), command.terminal.theme.successTextColor)
    command.output("=> $activity")
    command.output(command.terminal.activity.getString(R.string.activity_type, type))
    command.output(command.terminal.activity.getString(R.string.activity_participants, participants))
    command.output("-------------------------")
}

fun handleError(error: VolleyError, command: Command) {
    if (error is NoConnectionError) {
        command.output(command.terminal.activity.getString(R.string.no_internet_connection), command.terminal.theme.errorTextColor)
    }
    else {
        command.output(command.terminal.activity.getString(R.string.an_error_occurred_please_try_again),command.terminal.theme.errorTextColor)
    }
}
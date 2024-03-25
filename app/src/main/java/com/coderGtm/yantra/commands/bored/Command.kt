package com.coderGtm.yantra.commands.bored

import android.graphics.Typeface
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "bored",
        helpTitle = "bored",
        description = terminal.activity.getString(R.string.cmd_bored_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.bored_no_args), terminal.theme.errorTextColor)
            return
        }
        val url = "https://www.boredapi.com/api/activity/"
        val queue = Volley.newRequestQueue(terminal.activity)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                handleResponse(response, this@Command)
            },
            { error ->
                handleError(error, this@Command)
            })
        queue.add(stringRequest)
        output(terminal.activity.getString(R.string.fetching_random_activity), terminal.theme.resultTextColor, Typeface.ITALIC)
    }
}
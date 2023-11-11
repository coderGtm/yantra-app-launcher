package com.coderGtm.yantra.commands.time

import android.graphics.Typeface
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "time",
        helpTitle = "time",
        description = "Shows current local Date and Time"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'time' command does not take any parameters", terminal.theme.errorTextColor)
            return
        }
        val time = SimpleDateFormat("HH:mm:ss E d/M/y", Locale.getDefault()).format(Date())
        output(time)
    }
}
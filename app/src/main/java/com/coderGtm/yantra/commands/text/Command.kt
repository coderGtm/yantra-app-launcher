package com.coderGtm.yantra.commands.text

import android.content.Intent
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "text",
        helpTitle = terminal.activity.getString(R.string.cmd_text_title),
        description = terminal.activity.getString(R.string.cmd_text_help)
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.text_no_msg), terminal.theme.errorTextColor)
        }
        else {
            val msg = command.removePrefix(args[0])
            val  intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, msg.trim())
            intent.type = "text/plain"
            terminal.activity.startActivity(Intent.createChooser(intent,
                terminal.activity.getString(R.string.send_via)))
            output(terminal.activity.getString(R.string.text_broadcasted),terminal.theme.successTextColor)
        }
    }
}
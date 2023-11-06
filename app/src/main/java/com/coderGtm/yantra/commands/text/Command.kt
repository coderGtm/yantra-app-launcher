package com.coderGtm.yantra.commands.text

import android.content.Intent
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "text",
        description = "Broadcasts text message."
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify the message string.", terminal.theme.errorTextColor)
        }
        else {
            val msg = command.removePrefix(args[0])
            val  intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, msg.trim())
            intent.type = "text/plain"
            terminal.activity.startActivity(Intent.createChooser(intent, "Send via"))
            output("Text broadcasted",terminal.theme.successTextColor)
        }
    }
}
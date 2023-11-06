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
    override fun execute(flags: Map<String, String>, body: String) {
        if (flags.isNotEmpty()) {
            output("'text' command does not take any flags!", terminal.theme.errorTextColor)
        }
        if (body.isEmpty()) {
            output("Please specify the text to broadcast!", terminal.theme.errorTextColor)
            return
        }
        val msg = body
        val  intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, msg.trim())
        intent.type = "text/plain"
        terminal.activity.startActivity(Intent.createChooser(intent, "Send via"))
        output("Text broadcasted",terminal.theme.successTextColor)
    }
}
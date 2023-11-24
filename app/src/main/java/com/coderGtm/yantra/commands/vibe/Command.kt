package com.coderGtm.yantra.commands.vibe

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.vibrate

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "vibe",
        helpTitle = "calc <millis>",
        description = "Vibrates the device for specified duration (in milliseconds).\nExample: 'vibe 1000' does a 1 second vibration."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify vibration duration in milliseconds.", terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output("'vibe' command takes only 1 argument", terminal.theme.errorTextColor)
            return
        }
        val millis = args[1].toLongOrNull()
        if (millis == null) {
            output("Invalid usage. 'vibe' command takes only 1 argument: time to vibrate in milliseconds.",terminal.theme.errorTextColor)
            return
        }
        if (millis < 0) {
            output("Hold on wiz! How can your device vibrate in negative time? Only positive values allowed!",terminal.theme.errorTextColor)
            return
        }
        if (millis == 0L) {
            output("You want to vibrate for 0 milliseconds? Really?",terminal.theme.errorTextColor)
            return
        }
        vibrate(millis, terminal.activity)
    }
}
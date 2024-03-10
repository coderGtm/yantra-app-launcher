package com.coderGtm.yantra.commands.vibe

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.vibrate

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "vibe",
        helpTitle = "vibe <millis>",
        description = terminal.activity.getString(R.string.cmd_vibe_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_vibe_duration_in_ms), terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
            return
        }
        val millis = args[1].toLongOrNull()
        if (millis == null) {
            output(terminal.activity.getString(R.string.vibe_only_one_param),terminal.theme.errorTextColor)
            return
        }
        if (millis < 0) {
            output(terminal.activity.getString(R.string.vibe_negative),terminal.theme.errorTextColor)
            return
        }
        if (millis == 0L) {
            output(terminal.activity.getString(R.string.vibe_0),terminal.theme.errorTextColor)
            return
        }
        vibrate(millis, terminal.activity)
    }
}
package com.coderGtm.yantra.commands.pro

import android.graphics.Typeface
import com.coderGtm.yantra.PRO_VERSION_URL
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "pro",
        helpTitle = "pro",
        description = terminal.activity.getString(R.string.cmd_pro_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmd_takes_no_params, metadata.name), terminal.theme.errorTextColor)
            return
        }
        output(terminal.activity.getString(R.string.opening_pro_page), terminal.theme.resultTextColor, Typeface.ITALIC)
        openURL(PRO_VERSION_URL, terminal.activity)
    }
}
package com.coderGtm.yantra.commands.support

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SUPPORT_URL
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal
import java.util.Timer
import kotlin.concurrent.schedule

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "support",
        helpTitle = "support",
        description = terminal.activity.getString(R.string.cmd_support_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size > 1) {
            output(terminal.activity.getString(R.string.support_no_args), terminal.theme.errorTextColor, markdown = true)
            return
        }
        else {
            output("-------------------------", terminal.theme.warningTextColor)
            output(terminal.activity.getString(R.string.support_chunk_1), terminal.theme.resultTextColor, Typeface.BOLD)
            output(terminal.activity.getString(R.string.support_chunk_2), terminal.theme.resultTextColor)
            output("-------------------------", terminal.theme.warningTextColor)
            output(terminal.activity.getString(R.string.redirecting_to_support_page), terminal.theme.successTextColor, Typeface.ITALIC)
            Timer().schedule(5000) {
                openURL(SUPPORT_URL, terminal.activity)
            }
        }
    }
}
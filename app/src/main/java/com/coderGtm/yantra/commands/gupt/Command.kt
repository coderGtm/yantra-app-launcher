package com.coderGtm.yantra.commands.gupt

import android.content.Intent
import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.WebViewActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "gupt",
        helpTitle = terminal.activity.getString(R.string.cmd_gupt_title),
        description = terminal.activity.getString(R.string.cmd_gupt_help)
    )

    override fun execute(command: String) {
        output(terminal.activity.getString(R.string.initializing_g_u_p_t),terminal.theme.warningTextColor)
        output(terminal.activity.getString(R.string.getting_undercover_private_tab),terminal.theme.resultTextColor, Typeface.ITALIC)
        val cmdArray = command.split(" ")
        var url = "https://www.google.com"
        if (cmdArray.size > 1) {
            if (cmdArray.size > 2) {
                output(terminal.activity.getString(R.string.gupt_many_params), terminal.theme.errorTextColor)
                return
            }
            url = cmdArray[1].trim()
            if (url.startsWith("http://")) {
                url = url.removePrefix("http://")
            }
            if (!url.startsWith("https://")) {
                url = "https://$url"
            }
        }
        terminal.activity.startActivity(Intent(terminal.activity, WebViewActivity::class.java).putExtra("url", url))
        output(terminal.activity.getString(R.string.launched_g_u_p_t),terminal.theme.successTextColor)
    }
}
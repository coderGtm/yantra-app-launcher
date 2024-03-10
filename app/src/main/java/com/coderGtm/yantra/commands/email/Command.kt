package com.coderGtm.yantra.commands.email

import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "email",
        helpTitle = terminal.activity.getString(R.string.cmd_email_title),
        description = terminal.activity.getString(R.string.cmd_email_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.please_specify_an_email_address), terminal.theme.errorTextColor)
            return
        }
        val email = command.removePrefix(args[0]).trim()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email.trim()))
        }
        if (intent.resolveActivity(terminal.activity.packageManager) != null) {
            terminal.activity.startActivity(intent)
            output(terminal.activity.getString(R.string.launched_email_app),terminal.theme.successTextColor)
        }
        else {
            toast(terminal.activity.baseContext, terminal.activity.getString(R.string.could_not_launch_an_email_app))
        }
    }
}
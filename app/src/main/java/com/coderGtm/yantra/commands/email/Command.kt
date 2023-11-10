package com.coderGtm.yantra.commands.email

import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "email",
        helpTitle = "email <email-id>",
        description = "Opens email app with recipient set. Example: 'email coderGtm@gmail.com'"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify an Email address.", terminal.theme.errorTextColor)
            return
        }
        val email = command.removePrefix(args[0]).trim()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email.trim()))
        }
        if (intent.resolveActivity(terminal.activity.packageManager) != null) {
            terminal.activity.startActivity(intent)
            output("Opened email app...",terminal.theme.successTextColor)
        }
        else {
            toast(terminal.activity.baseContext, "Could not open an email app.")
        }
    }
}
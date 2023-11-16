package com.coderGtm.yantra.commands.cmdrequest

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "cmdrequest",
        helpTitle = "cmdrequest",
        description = "In case you want any new commands to be added to Yantra Launcher, run this command to open email template for command request. Note that it is not guaranteed that the command request will be accepted but I'll try my best to see if I could accommodate it in the upcoming versions of Yantra Launcher, if suitable."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'cmdrequest' command does not take any parameters.", terminal.theme.errorTextColor)
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf("coderGtm@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Command request for Yantra Launcher")
            putExtra(Intent.EXTRA_TEXT, "I would like to request the following command for Yantra Launcher:\n\n[COMMAND]\n\n[DESCRIPTION]\n\n[EXAMPLE]\n\n[ANYTHING ELSE]")
        }
        if (intent.resolveActivity(terminal.activity.packageManager) != null) {
            terminal.activity.startActivity(intent)
        } else {
            MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog).setTitle("Oops!")
                .setMessage("Could not open an email app. Please send the mail to coderGtm@gmail.com with title 'Command request for Yantra Launcher'")
                .setPositiveButton("OK") { dialog, _ ->
                    //copy title to clipboard
                    val clipboard = terminal.activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Command request for Yantra Launcher", "Command request for Yantra Launcher")
                    clipboard.setPrimaryClip(clip)
                    toast(terminal.activity.baseContext, "Copied title to clipboard")
                    dialog.dismiss()
                }
                .show()
        }
    }
}
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
        description = terminal.activity.getString(R.string.cmd_cmdrequest_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmdrequest_no_args), terminal.theme.errorTextColor)
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
            MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle(terminal.activity.getString(R.string.oops))
                .setMessage(terminal.activity.getString(R.string.send_manual_mail_with_title, "Command request for Yantra Launcher"))
                .setPositiveButton(terminal.activity.getString(R.string.ok)) { dialog, _ ->
                    //copy title to clipboard
                    val clipboard = terminal.activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(terminal.activity.getString(R.string.command_request_for_yantra_launcher), terminal.activity.getString(R.string.command_request_for_yantra_launcher))
                    clipboard.setPrimaryClip(clip)
                    toast(terminal.activity.baseContext, terminal.activity.getString(R.string.copied_title_to_clipboard))
                    dialog.dismiss()
                }
                .show()
        }
    }
}
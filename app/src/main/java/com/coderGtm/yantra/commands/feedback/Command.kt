package com.coderGtm.yantra.commands.feedback

import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.PLAY_STORE_URL
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "feedback",
        helpTitle = "feedback",
        description = terminal.activity.getString(R.string.cmd_feedback_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.command_does_not_take_any_parameters, metadata.name), terminal.theme.errorTextColor)
            return
        }
        MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
            .setTitle(terminal.activity.getString(R.string.feedback))
            .setMessage(terminal.activity.getString(R.string.feedback_description))
            .setPositiveButton(terminal.activity.getString(R.string.email)) { _, _ ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // only email apps should handle this
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("coderGtm@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback for Yantra Launcher")
                }
                if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                    terminal.activity.startActivity(intent)
                } else {
                    MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog).setTitle(terminal.activity.getString(R.string.oops))
                        .setMessage(terminal.activity.getString(R.string.send_manual_mail_with_title, "Feedback for Yantra Launcher"))
                        .setPositiveButton(terminal.activity.getString(R.string.ok), null).show()
                }
            }
            .setNegativeButton(terminal.activity.getString(R.string.play_store)) { _, _ ->
                openURL(PLAY_STORE_URL, terminal.activity)
            }
            .setNeutralButton(terminal.activity.getString(R.string.cancel), null)
            .show()
    }
}
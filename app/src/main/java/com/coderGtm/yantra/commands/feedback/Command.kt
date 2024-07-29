package com.coderGtm.yantra.commands.feedback

import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.getStoreUrl
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

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
        YantraLauncherDialog(terminal.activity).showInfo(
            title = terminal.activity.getString(R.string.feedback),
            message = terminal.activity.getString(R.string.feedback_description),
            positiveButton = terminal.activity.getString(R.string.email),
            negativeButton = terminal.activity.getString(R.string.play_store),
            positiveAction = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // only email apps should handle this
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("coderGtm@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback for Yantra Launcher")
                }
                if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                    terminal.activity.startActivity(intent)
                } else {
                    YantraLauncherDialog(terminal.activity).showInfo(
                        title = terminal.activity.getString(R.string.oops),
                        message = terminal.activity.getString(R.string.send_manual_mail_with_title, "Feedback for Yantra Launcher"),
                        positiveButton = terminal.activity.getString(R.string.ok)
                    )
                }
            },
            negativeAction = {
                openURL(getStoreUrl(terminal.activity), terminal.activity)
            }
        )
    }
}
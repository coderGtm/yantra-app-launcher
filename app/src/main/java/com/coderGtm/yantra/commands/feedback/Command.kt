package com.coderGtm.yantra.commands.feedback

import android.content.Intent
import android.net.Uri
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
        description = "Clears the console"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'feedback' command does not take any parameters.", terminal.theme.errorTextColor)
            return
        }
        MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
            .setTitle("Feedback")
            .setMessage("Thank you for choosing to give feedback! Your feedback fuels my motivation and helps me improve the app.\n\nYou can send feedback my mailing me at coderGtm@gmail.com or by giving a review on the Play Store.")
            .setPositiveButton("Email") { _, _ ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // only email apps should handle this
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("coderGtm@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback for Yantra Launcher")
                }
                if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                    terminal.activity.startActivity(intent)
                } else {
                    MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog).setTitle("Oops!")
                        .setMessage("Could not open an email app. Please send the mail to coderGtm@gmail.com")
                        .setPositiveButton("OK", null).show()
                }
            }
            .setNegativeButton("Play Store") { _, _ ->
                openURL("https://play.google.com/store/apps/details?id=com.coderGtm.yantra", terminal.activity)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
}
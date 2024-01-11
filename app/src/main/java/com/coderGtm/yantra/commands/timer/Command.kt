package com.coderGtm.yantra.commands.timer

import android.content.Intent
import android.graphics.Typeface
import android.provider.AlarmClock
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal


class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "timer",
        helpTitle = "timer [length] [message]",
        description = "Used to create a countdown timer using the default app on your device. Use without args to open the timer app. The length must be in seconds and ranges from 1 to 86400 (24 hrs). It can optionally be followed by a message string to display in timer.\nExamples:\n'timer 60' sets timer for 1 minute\n'timer 3600 Take out the trash' sets timer for 1 hour with a message to display."
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.isEmpty()) {
            output("No length provided. Opening timer app.", terminal.theme.resultTextColor)
            terminal.activity.startActivity(Intent(AlarmClock.ACTION_SET_TIMER))
        }
        else {
            output(":: Parsing length string...", terminal.theme.resultTextColor, Typeface.ITALIC)
            val lengthString = args.first().trim()
            if (!isValidLengthString(lengthString)) {
                output("Invalid length string provided. It must be a number between 1 and 86400.", terminal.theme.errorTextColor)
                return
            }
            val length = lengthString.toInt()
            var msg = ""

            if (args.size > 1) {
                output(":: Parsing message string...", terminal.theme.resultTextColor, Typeface.ITALIC)
                // get the original message string from the command
                msg = command.substringAfter(lengthString).trim()
            }

            output("=> Setting timer...", terminal.theme.resultTextColor)
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, length)
                putExtra(AlarmClock.EXTRA_MESSAGE, msg)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            }
            if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                terminal.activity.startActivity(intent)
                output("Timer set successfully.", terminal.theme.successTextColor, Typeface.BOLD)
            }
            else {
                output("No supported timer app found on your device.", terminal.theme.errorTextColor)
            }
        }
    }
}
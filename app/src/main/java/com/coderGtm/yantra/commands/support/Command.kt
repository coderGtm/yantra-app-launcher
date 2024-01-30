package com.coderGtm.yantra.commands.support

import android.graphics.Typeface
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "support",
        helpTitle = "support [support-index]",
        description = "Support Yantra Launcher."
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size == 1) {
            output("-------------------------", terminal.theme.warningTextColor)
            output("Yantra Launcher is a powerful and free open-source tool to boost your productivity and be cool. Developing this launcher and maintaining it genuinely demands a lot of efforts, resources and invaluable time. If Yantra Launcher has made any positive difference in your life, I appeal you to consider manifesting your gratefulness by supporting the project.\n\nYou can make any of the following donations as many time as you like it :)\n\n")
            output("0:  Tiny", terminal.theme.resultTextColor, Typeface.BOLD)
            output("1:  Mini", terminal.theme.resultTextColor, Typeface.BOLD)
            output("2:  Small", terminal.theme.resultTextColor, Typeface.BOLD)
            output("3:  Kind", terminal.theme.resultTextColor, Typeface.BOLD)
            output("4:  Generous", terminal.theme.resultTextColor, Typeface.BOLD)
            output("\n\nUse the command with the index of support to initiate payment flow. For example, 'support 2'", terminal.theme.successTextColor)
            output("-------------------------", terminal.theme.warningTextColor)
            output("Your donation helps me fund the project and keep the app free and Ad-free for everyone.", terminal.theme.resultTextColor, Typeface.BOLD_ITALIC)
        }
        else if (args.size == 2) {
            if (args[1].trim().toIntOrNull() == null) {
                output("Please pass an integer parameter for support index. For example:\nsupport 2", terminal.theme.errorTextColor)
                return
            }

            val supportIndex = args[1].trim().toInt()

            if (supportIndex < 0 || supportIndex > 4) {
                output("Invalid support index. Use an integer from 0 to 4. See 'support' without any args to get details.", terminal.theme.errorTextColor)
                return
            }

            output(":: Starting Support flow...", terminal.theme.resultTextColor, Typeface.ITALIC)
            (terminal.activity as MainActivity).initializeProductPurchase("donate$supportIndex")
        }
    }
}
package com.coderGtm.yantra.commands.support

import android.graphics.Typeface
import com.coderGtm.yantra.SUPPORT_URL
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal
import java.util.Timer
import kotlin.concurrent.schedule

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "support",
        helpTitle = "support",
        description = "About supporting Yantra Launcher."
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size > 1) {
            output("'support' command does not take any **arguments**. _It's a friendly command... :)_", terminal.theme.errorTextColor, markdown = true)
            return
        }
        else {
            output("-------------------------", terminal.theme.warningTextColor)
            output("Yantra Launcher is a powerful and free open-source tool to boost your productivity and be cool. Developing this launcher and maintaining it genuinely demands a lot of efforts, resources and invaluable time. If Yantra Launcher has made any positive difference in your life, I appeal you to check ou the Support section on the project's GitHub page.", terminal.theme.resultTextColor, Typeface.BOLD)
            output("\nRemember, Yantra Launcher is completely free and open-source. You can support the developer by contributing to the project, reporting issues, suggesting features, and sharing it with your friends and family.", terminal.theme.resultTextColor)
            output("-------------------------", terminal.theme.warningTextColor)
            output(":: Redirecting you to the Support section on the project's GitHub page...", terminal.theme.successTextColor, Typeface.ITALIC)
            Timer().schedule(5000) {
                openURL(SUPPORT_URL, terminal.activity)
            }
        }
    }
}
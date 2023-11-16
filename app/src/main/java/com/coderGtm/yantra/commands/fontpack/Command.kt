package com.coderGtm.yantra.commands.fontpack

import android.graphics.Typeface
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "fontpack",
        helpTitle = "fontpack",
        description = "Used to purchase or check purchase status of 'Font Pack' for Yantra Launcher."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'fontpack' command does not take any parameters.", terminal.theme.errorTextColor)
            return
        }
        if (terminal.preferenceObject.getBoolean("fontpack___purchased",false)) {
            output("Font Pack is already purchased. You can change the terminal font from settings.",terminal.theme.successTextColor)
        }
        else {
            output("'fontpack' is not purchased!",terminal.theme.errorTextColor)
            output("--------------------------",terminal.theme.warningTextColor)
            output("Font Pack is an add-on for Yantra Launcher that lets you use any font from the entire collection of Google Fonts of more than 1550 fonts for your Yantra Launcher Terminal",terminal.theme.resultTextColor, Typeface.BOLD)
            output("--------------------------",terminal.theme.warningTextColor)
            val mainAct = terminal.activity as MainActivity
            mainAct.initializeProductPurchase("fontpack")
        }
    }
}
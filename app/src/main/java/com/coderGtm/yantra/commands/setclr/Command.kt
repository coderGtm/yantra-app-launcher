package com.coderGtm.yantra.commands.setclr

import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.isValidHexCode
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "setclr",
        helpTitle = "setclr <color>/-1",
        description = "Change the terminal text color to the specified 8 digit color code (without the #) or reset to default color. Color set via this color will override the theme color.",
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size < 2) {
            output(terminal.activity.getString(R.string.command_takes_at_least_1_parameter_0_provided, metadata.name), terminal.theme.errorTextColor, null)
            return
        }
        if (args.size > 3) {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
            return
        }
        val colorParam = args[1].trim()
        if (colorParam == "-1") {
            terminal.dominantFontColor = null
            toast(terminal.activity.baseContext, terminal.activity.getString(R.string.terminal_text_color_reset))
            return
        }
        if (!isValidHexCode(colorParam)) {
            output(terminal.activity.getString(R.string.invalid_hex_code), terminal.theme.errorTextColor)
            return
        }
        terminal.dominantFontColor = Color.parseColor("#$colorParam")
        toast(terminal.activity.baseContext, terminal.activity.getString(R.string.terminal_text_color_set_to, colorParam))
    }
}
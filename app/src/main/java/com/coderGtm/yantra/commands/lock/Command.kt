package com.coderGtm.yantra.commands.lock

import android.graphics.Typeface
import android.os.Build
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "lock",
        helpTitle = "lock",
        description = terminal.activity.getString(R.string.cmd_lock_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmd_takes_no_params, metadata.name), terminal.theme.errorTextColor)
            return
        }
        output(terminal.activity.getString(R.string.attempting_to_lock_device), terminal.theme.resultTextColor, Typeface.ITALIC)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // lock by Accessibility service (for Android 9 and above)
            lockDeviceByAccessibilityService(terminal.activity, terminal.binding)
        }
        else {
            // lock by admin (for Android 8 and below)
            lockDeviceByAdmin(terminal.activity)
        }
    }
}
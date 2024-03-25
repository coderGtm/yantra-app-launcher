package com.coderGtm.yantra.commands.sleep

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.util.Timer
import kotlin.concurrent.schedule

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "sleep",
        helpTitle = "sleep <millis>",
        description = terminal.activity.getString(R.string.cmd_sleep_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_sleep_in_ms), terminal.theme.errorTextColor)
            return
        }
        if (args.size > 3) {
            output(terminal.activity.getString(R.string.sleep_1_param), terminal.theme.errorTextColor)
            return
        }
        val milliseconds = args[1].toLongOrNull()
        if (milliseconds == null) {
            output(terminal.activity.getString(R.string.sleep_1_param),terminal.theme.errorTextColor)
            return
        }
        terminal.isSleeping = true
        terminal.activity.runOnUiThread {
            terminal.binding.terminalOutput.addView(terminal.wakeBtn)
        }
        terminal.wakeBtn.updateLayoutParams { width = ViewGroup.LayoutParams.WRAP_CONTENT }
        terminal.binding.cmdInput.isEnabled = false
        terminal.sleepTimer = Timer().schedule(milliseconds) {
            terminal.isSleeping = false
            terminal.activity.runOnUiThread {
                terminal.binding.terminalOutput.removeView(terminal.wakeBtn)
                terminal.binding.cmdInput.isEnabled = true
                terminal.executeCommandsInQueue()
            }
        }
    }
}
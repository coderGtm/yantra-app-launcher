package com.coderGtm.yantra.commands.sleep

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.util.Timer
import kotlin.concurrent.schedule

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "sleep",
        helpTitle = "sleep <millis>",
        description = "Pauses Yantra Launcher for specified milliseconds.\nUsage: 'sleep numOfMilliseconds'\nExample: 'sleep 5000'"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify sleep duration in milliseconds.", terminal.theme.errorTextColor)
            return
        }
        val milliseconds = args[1].toLongOrNull()
        if (milliseconds == null) {
            output("Invalid usage. 'sleep' command takes only 1 argument: time to sleep in milliseconds.",terminal.theme.errorTextColor)
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
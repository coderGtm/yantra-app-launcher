package com.coderGtm.yantra.commands.sleep

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.util.Timer
import kotlin.concurrent.schedule

class Command(terminal: Terminal) : BaseCommand(terminal) {
    private var wakeActionId: String? = null

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
            showWakeAction()
        }
        terminal.binding.cmdInput.isEnabled = false
        terminal.sleepTimer = Timer().schedule(milliseconds) {
            terminal.isSleeping = false
            terminal.activity.runOnUiThread {
                hideWakeAction()
                terminal.binding.cmdInput.isEnabled = true
                terminal.executeCommandsInQueue()
            }
        }
    }

    private fun showWakeAction() {
        if (wakeActionId != null) {
            return
        }
        wakeActionId = terminal.binding.addActionOutput(
            text = "Break",
            color = terminal.theme.errorTextColor,
            underlined = true,
            fontSize = terminal.preferenceObject.getInt("fontSize", 16).toFloat(),
        ) {
            terminal.sleepTimer?.cancel()
            terminal.isSleeping = false
            hideWakeAction()
            output("Yantra Launcher awakened mid-sleep (~_^)", terminal.theme.errorTextColor)
            terminal.binding.cmdInput.isEnabled = true
            terminal.executeCommandsInQueue()
        }
    }

    private fun hideWakeAction() {
        wakeActionId?.let(terminal.binding::removeOutputItem)
        wakeActionId = null
    }
}
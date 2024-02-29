package com.coderGtm.yantra.commands.termux

import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "termux",
        helpTitle = terminal.activity.getString(R.string.cmd_termux_title),
        description = terminal.activity.getString(R.string.cmd_termux_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        val cmd = command.trim().removePrefix(args[0]).trim()
        if (cmd == "") {
            output(terminal.activity.getString(R.string.termux_invalid_cmd), terminal.theme.errorTextColor)
            return
        }
        // check if termux is installed
        for (app in terminal.appList) {
            if (app.packageName == "com.termux") {
                // termux is installed
                if (ActivityCompat.checkSelfPermission(terminal.activity, "com.termux.permission.RUN_COMMAND") != PackageManager.PERMISSION_GRANTED) {
                    output(terminal.activity.getString(R.string.grant_termux_permission), terminal.theme.warningTextColor)
                    ActivityCompat.requestPermissions(terminal.activity, arrayOf("com.termux.permission.RUN_COMMAND"), PermissionRequestCodes.TERMUX_RUN_COMMAND.code)
                    return
                }
                val cmdName = cmd.split(" ")[0].trim()
                var cmdArgs = arrayOf<String>()
                if (cmd.split(" ").size > 1) {
                    cmdArgs = cmd.split(" ").drop(1).toTypedArray()
                }
                val cmdPath = terminal.preferenceObject.getString("termuxCmdPath", "/data/data/com.termux/files/usr/bin/")
                val cmdWorkDir = terminal.preferenceObject.getString("termuxCmdWorkDir", "/data/data/com.termux/files/home/")
                val cmdBackground = terminal.preferenceObject.getBoolean("termuxCmdBackground", false)
                val cmdSessionAction = terminal.preferenceObject.getInt("termuxCmdSessionAction", 0)
                val intent = Intent("com.termux.RUN_COMMAND").apply {
                    setClassName("com.termux", "com.termux.app.RunCommandService")
                    putExtra("com.termux.RUN_COMMAND_PATH", cmdPath + cmdName)
                    putExtra("com.termux.RUN_COMMAND_ARGUMENTS", cmdArgs)
                    putExtra("com.termux.RUN_COMMAND_WORKDIR", cmdWorkDir)
                    putExtra("com.termux.RUN_COMMAND_BACKGROUND", cmdBackground)
                    putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", cmdSessionAction)
                }
                if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                    output(terminal.activity.getString(R.string.running_command_in_termux),terminal.theme.successTextColor)
                    try {
                        terminal.activity.startService(intent)
                    }
                    catch (e: Exception) {
                        output(terminal.activity.getString(R.string.termux_cmd_error, e.message),terminal.theme.errorTextColor)
                    }
                }
                else {
                    output(terminal.activity.getString(R.string.termux_cmd_error, terminal.activity.getString(R.string.termux_not_installed)),terminal.theme.errorTextColor)
                }
                return
            }
        }
        output(terminal.activity.getString(R.string.termux_not_installed), terminal.theme.errorTextColor)
    }
}
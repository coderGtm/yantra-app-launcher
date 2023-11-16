package com.coderGtm.yantra.commands.termux

import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "termux",
        helpTitle = "termux [cmd] [args]",
        description = "Runs given command in termux if it is installed. Note that the 'allow-external-apps' property must be set to true in ~/.termux/termux.properties in Termux app and RUN_COMMAND permission must be given from Yantra Launcher's settings page. Also, termux session is closed as soon as command is terminated due to API restrictions.\nUsage: termux cmd_name [args]\nExample: 'termux top -n 5\nTo resolve any issues still occurring, ask in Discord Community or mail me at coderGtm@gmail.com."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        val cmd = command.trim().removePrefix(args[0]).trim()
        if (cmd == "") {
            output("Invalid command. See 'help termux' for usage info", terminal.theme.errorTextColor)
            return
        }
        // check if termux is installed
        for (app in terminal.appList) {
            if (app.packageName == "com.termux") {
                // termux is installed
                if (ActivityCompat.checkSelfPermission(terminal.activity, "com.termux.permission.RUN_COMMAND") != PackageManager.PERMISSION_GRANTED) {
                    output("You need to grant the Termux Run Command Permission to Yantra Launcher for this command to work!", terminal.theme.warningTextColor)
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
                    output("Running command in Termux...",terminal.theme.successTextColor)
                    try {
                        terminal.activity.startService(intent)
                    }
                    catch (e: Exception) {
                        output("Could not run command in Termux. Error: ${e.message}",terminal.theme.errorTextColor)
                    }
                }
                else {
                    output("Could not run command in Termux.",terminal.theme.errorTextColor)
                }
                return
            }
        }
        output("Termux is not installed.",terminal.theme.errorTextColor)
    }
}
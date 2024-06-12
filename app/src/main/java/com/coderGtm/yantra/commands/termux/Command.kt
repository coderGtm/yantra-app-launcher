package com.coderGtm.yantra.commands.termux

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.services.TermuxCommandService
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
                val cmdBackground = terminal.preferenceObject.getBoolean("termuxCmdBackground", true)
                val cmdSessionAction = terminal.preferenceObject.getInt("termuxCmdSessionAction", 0).toString()
                val intent = Intent("com.termux.RUN_COMMAND").apply {
                    setClassName("com.termux", "com.termux.app.RunCommandService")
                    putExtra("com.termux.RUN_COMMAND_PATH", cmdPath + cmdName)
                    putExtra("com.termux.RUN_COMMAND_ARGUMENTS", cmdArgs)
                    putExtra("com.termux.RUN_COMMAND_WORKDIR", cmdWorkDir)
                    putExtra("com.termux.RUN_COMMAND_BACKGROUND", cmdBackground)
                    putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", cmdSessionAction)
                }

                // Create the intent for the IntentService class that should be sent the result by TermuxService
                val termuxCommandServiceIntent = Intent(
                    terminal.activity,
                    TermuxCommandService::class.java
                )


                // Generate a unique execution id for this execution command
                val executionId: Int = TermuxCommandService.getNextExecutionId()


                // Optional put an extra that uniquely identifies the command internally for your app.
                // This can be an Intent extra as well with more extras instead of just an int.
                termuxCommandServiceIntent.putExtra(
                    TermuxCommandService.EXTRA_EXECUTION_ID,
                    executionId
                )


                // Create the PendingIntent that will be used by TermuxService to send result of
                // commands back to the IntentService
                // Note that the requestCode (currently executionId) must be unique for each pending
                // intent, even if extras are different, otherwise only the result of only the first
                // execution will be returned since pending intent will be cancelled by android
                // after the first result has been sent back via the pending intent and termux
                // will not be able to send more.
                val pendingIntent = PendingIntent.getService(
                    terminal.activity, executionId,
                    termuxCommandServiceIntent,
                    PendingIntent.FLAG_ONE_SHOT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
                )
                intent.putExtra("com.termux.RUN_COMMAND_PENDING_INTENT", pendingIntent)

                try {
                    // Send command intent for execution
                    output(terminal.activity.getString(R.string.running_command_in_termux),terminal.theme.successTextColor)
                    terminal.activity.startService(intent)
                } catch (e: java.lang.Exception) {
                    output(terminal.activity.getString(R.string.termux_cmd_error, e.message),terminal.theme.errorTextColor)
                }
                return
            }
        }
        output(terminal.activity.getString(R.string.termux_not_installed), terminal.theme.errorTextColor)
    }
}
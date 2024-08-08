package com.coderGtm.yantra.commands.pwd

import android.os.Environment
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "pwd",
        helpTitle = "pwd",
        description = terminal.activity.getString(R.string.cmd_pwd_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmd_takes_no_params, metadata.name), terminal.theme.errorTextColor)
            return
        }
        var wd = terminal.workingDir
        wd = if (wd.isBlank()) {
            Environment.getExternalStorageDirectory().absolutePath
        } else {
            "${Environment.getExternalStorageDirectory().absolutePath}$wd"
        }
        output(wd)
    }
}
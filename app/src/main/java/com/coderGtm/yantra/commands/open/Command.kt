package com.coderGtm.yantra.commands.open

import android.os.Environment
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.io.File

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "open",
        helpTitle = terminal.activity.getString(R.string.cmd_open_title),
        description = terminal.activity.getString(R.string.cmd_open_help)
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_file_to_open), terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim()

        val fullPath = Environment.getExternalStorageDirectory().absolutePath + "${terminal.workingDir}/$name"
        val file = File(fullPath)

        if (isExists(fullPath)) {
            openFiles(file,this@Command)
            return
        }

        output(terminal.activity.getString(R.string.error_not_a_file, fullPath), terminal.theme.errorTextColor)
    }
}
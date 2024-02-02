package com.coderGtm.yantra.commands.file

import android.os.Environment
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.getUserName
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.io.File

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "file",
        helpTitle = "file [file name]",
        description = "Opens specified file."
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify an file to open.", terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim().lowercase()

        val path = terminal.binding.username.text.toString().substring(
            getUserNamePrefix(terminal.preferenceObject).length +
                    getUserName(terminal.preferenceObject).length
        ).dropLast(1)

        val fullPath = Environment.getExternalStorageDirectory().absolutePath + "$path/$name"
        println(fullPath)
        val file = File(fullPath)

        if (isExists(fullPath)) {
            openFiles(file,this@Command)
            return
        }

        output("Error! File not found, please type or select correct file.", terminal.theme.errorTextColor)
    }
}
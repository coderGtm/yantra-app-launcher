package com.coderGtm.yantra.commands.ls

import android.os.Environment
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.getUserName
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.io.File

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "ls",
        helpTitle = "ls",
        description = "ls"
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.isNotEmpty()) {
            output("Error! No arguments provided", terminal.theme.errorTextColor)
            return
        }

        val path = terminal.binding.username.text.toString().substring(
            getUserNamePrefix(terminal.preferenceObject).length +
                    getUserName(terminal.preferenceObject).length
        ).dropLast(1)

        val files = File(Environment.getExternalStorageDirectory().absolutePath + path).listFiles()

        if (files == null) {
            output("Empty", terminal.theme.resultTextColor)
            return
        }

        data class AllFiles(val name: String)
        val fullList = mutableListOf<AllFiles>()

        for (file in files) {
            fullList.add(AllFiles(file.name))
        }

        fullList.sortBy { it.name }
        for (obj in fullList) {
            output(obj.name, terminal.theme.resultTextColor)
        }
    }
}
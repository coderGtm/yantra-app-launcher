package com.coderGtm.yantra.commands.cd

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.getUserName
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "cd",
        helpTitle = "cd [path]",
        description = "cd"
    )

    var path: String = "/"

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.isEmpty()) {
            output("Error! No path provided", terminal.theme.errorTextColor)
            return
        }

        if (!checkPermission()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION) // this check in Helper.kt in checkPermission()
            val uri = Uri.fromParts("package", terminal.activity.packageName, null)
            intent.data = uri
            terminal.activity.startActivity(intent)

            output("Error! Permission needed", terminal.theme.errorTextColor)
            return
        }

        val split = command.substring(3).split('/')
        var pathN = path
        for ( i in split ) {
            if (i == "..") {
                pathN = pathN.dropLast(pathN.split('/')[pathN.split('/').size - 1].length + 1)
            } else {
                pathN += "/$i"
            }
        }

        println(pathN)

        val newPath = getPathIfExists(pathN)

        if (newPath != null) {
            path = newPath
            list(path, this@Command)
            terminal.binding.username.text = getUserNamePrefix(terminal.preferenceObject) + getUserName(terminal.preferenceObject) + path + ">"
            return
        }

        output("Error! Path not exist", terminal.theme.errorTextColor)
        return
    }
}
package com.coderGtm.yantra.commands.cd

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
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

        if (!checkPermission(this@Command)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", terminal.activity.packageName, null)
                intent.data = uri
                terminal.activity.startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(terminal.activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1230)
            }

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

        val newPath = getPathIfExists(pathN)

        if (newPath != null) {
            path = newPath
            terminal.binding.username.text = getUserNamePrefix(terminal.preferenceObject) + getUserName(terminal.preferenceObject) + path + ">"
            return
        }

        output("Error! Path not exist", terminal.theme.errorTextColor)
        return
    }
}
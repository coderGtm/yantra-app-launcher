package com.coderGtm.yantra.commands.ls

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
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
        description = "Lists all files in the current directory"
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.isNotEmpty()) {
            output("Error! 'ls' command does not take any arguments", terminal.theme.errorTextColor)
            return
        }

        if (!checkPermission(this@Command)) {
            output("File Permission Missing!", terminal.theme.warningTextColor)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", terminal.activity.packageName, null)
                intent.data = uri
                terminal.activity.startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(terminal.activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PermissionRequestCodes.STORAGE.code)
            }
            return
        }

        val path = terminal.binding.username.text.toString().substring(
            getUserNamePrefix(terminal.preferenceObject).length +
                    getUserName(terminal.preferenceObject).length
        ).dropLast(1)

        val files = File(Environment.getExternalStorageDirectory().absolutePath + path).listFiles()

        if (files == null) {
            return
        }

        val fullList = mutableListOf<String>()

        for (file in files) {
            fullList.add(file.name)
        }

        fullList.sort()
        for (obj in fullList) {
            output(obj, terminal.theme.resultTextColor)
        }
    }

    private fun checkPermission(command: Command): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(command.terminal.activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
}
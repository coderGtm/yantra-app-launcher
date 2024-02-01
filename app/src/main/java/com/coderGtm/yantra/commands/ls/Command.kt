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

    private fun checkPermission(command: Command): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(command.terminal.activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }
}
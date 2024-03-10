package com.coderGtm.yantra.commands.ls

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.models.DirectoryContents
import com.coderGtm.yantra.terminal.Terminal
import java.io.File

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "ls",
        helpTitle = "ls [-a]",
        description = terminal.activity.getString(R.string.cmd_ls_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)
        var showHidden = false

        if (args.size == 1) {
            if (args.first().trim() == "-a") {
                showHidden = true
            }
            else {
                output(terminal.activity.getString(R.string.ls_invalid_arg), terminal.theme.errorTextColor)
                return
            }
        }
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.ls_many_args), terminal.theme.errorTextColor)
            return
        }

        if (!checkPermission(this@Command)) {
            output(terminal.activity.getString(R.string.feature_permission_missing, terminal.activity.getString(R.string.file)), terminal.theme.warningTextColor)

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

        val files = File(Environment.getExternalStorageDirectory().absolutePath + terminal.workingDir).listFiles()

        if (files == null) {
            return
        }

        val fullList = mutableListOf<DirectoryContents>()

        for (file in files) {
            fullList.add(
                DirectoryContents(
                    name = file.name,
                    isDirectory = file.isDirectory,
                    isHidden = file.isHidden
                )
            )
        }

        fullList.sortBy { it.name }
        for (obj in fullList) {
            if (obj.isHidden && !showHidden) {
                continue
            }
            if (obj.isDirectory) {
                output(obj.name, terminal.theme.warningTextColor, Typeface.BOLD)
            }
            else {
                output(obj.name, terminal.theme.resultTextColor)
            }
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
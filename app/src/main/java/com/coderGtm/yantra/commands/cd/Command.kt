package com.coderGtm.yantra.commands.cd

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "cd",
        helpTitle = terminal.activity.getString(R.string.cmd_cd_title),
        description = terminal.activity.getString(R.string.cmd_cd_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.isEmpty()) {
            output(terminal.activity.getString(R.string.please_provide_a_path), terminal.theme.errorTextColor)
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

        val split = command.substring(3).split('/')
        var pathN = terminal.workingDir
        for ( i in split ) {
            if (i == "..") {
                pathN = pathN.dropLast(pathN.split('/')[pathN.split('/').size - 1].length + 1)
            } else {
                pathN += "/$i"
            }
        }

        val newPath = getPathIfExists(pathN)

        if (newPath != null) {
            terminal.workingDir = newPath
            terminal.setPromptText()
            return
        }

        output(terminal.activity.getString(R.string.error_no_matching_directory_found), terminal.theme.errorTextColor)
        return
    }
}
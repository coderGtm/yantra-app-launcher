package com.coderGtm.yantra.commands.call

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.contactsManager
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "call",
        helpTitle = terminal.activity.getString(R.string.cmd_call_title),
        description = terminal.activity.getString(R.string.cmd_call_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.call_no_args), terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim()
        if (ContextCompat.checkSelfPermission(terminal.activity.baseContext,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            output(terminal.activity.getString(R.string.feature_permission_missing, terminal.activity.getString(R.string.contacts)), terminal.theme.warningTextColor)
            ActivityCompat.requestPermissions(terminal.activity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                PermissionRequestCodes.CONTACTS.code)
        }
        if (ContextCompat.checkSelfPermission(terminal.activity.baseContext,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            output(terminal.activity.getString(R.string.feature_permission_missing, terminal.activity.getString(R.string.call)), terminal.theme.warningTextColor)
            ActivityCompat.requestPermissions(terminal.activity,
                arrayOf(Manifest.permission.CALL_PHONE),
                PermissionRequestCodes.CALL.code)
        }
        else {
            output(terminal.activity.getString(R.string.resolving_name))
            Thread {
                contactsManager(terminal, true, name.lowercase())
            }.start()
        }
    }
}
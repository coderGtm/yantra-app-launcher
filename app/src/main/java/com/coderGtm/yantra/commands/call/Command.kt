package com.coderGtm.yantra.commands.call

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.contactsManager
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "call",
        helpTitle = "call [name | number]",
        description = "Calls specified contact name. If contact name is not found then the raw input (considered as phone number) is called."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify a contact name or phone number to call", terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim()
        if (ContextCompat.checkSelfPermission(terminal.activity.baseContext,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            output("Contacts Permission missing!",terminal.theme.warningTextColor)
            ActivityCompat.requestPermissions(terminal.activity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                PermissionRequestCodes.CONTACTS.code)
        }
        if (ContextCompat.checkSelfPermission(terminal.activity.baseContext,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            output("Call Permission missing!",terminal.theme.warningTextColor)
            ActivityCompat.requestPermissions(terminal.activity,
                arrayOf(Manifest.permission.CALL_PHONE),
                PermissionRequestCodes.CALL.code)
        }
        else {
            output("Resolving name...")
            Thread {
                contactsManager(terminal, true, name.lowercase())
            }.start()
        }
    }
}
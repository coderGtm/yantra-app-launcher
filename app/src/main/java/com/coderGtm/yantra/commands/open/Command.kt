package com.coderGtm.yantra.commands.open

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.checkCroissantPermission
import com.coderGtm.yantra.isCroissantInstalled
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "open",
        helpTitle = terminal.activity.getString(R.string.cmd_open_title),
        description = terminal.activity.getString(R.string.cmd_open_help)
    )
    override fun execute(command: String) {
        if (!isCroissantInstalled(terminal)) {
            val releasePageUrl = "https://github.com/Anready/Croissant/releases"
            val appName = "Croissant"
            output(
                terminal.activity.getString(R.string.this_cmd_handled_via_third_party_app, appName, releasePageUrl), terminal.theme.warningTextColor, null, true)
            return
        }

        if (!checkCroissantPermission(terminal.activity)) {
            val appName = "Croissant"
            output(terminal.activity.getString(R.string.app_does_not_have_reqd_perms, appName), terminal.theme.warningTextColor)
            return
        }

        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_file_to_open), terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim()

        val fullPath = "${terminal.workingDir}/$name"

        if (isPathExist(this@Command, fullPath)) {
            openFile(fullPath,this@Command)
            return
        }

        output(terminal.activity.getString(R.string.error_not_a_file, fullPath), terminal.theme.errorTextColor)
    }
}
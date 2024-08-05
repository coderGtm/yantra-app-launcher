package com.coderGtm.yantra.commands.cd

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.checkCroissantPermission
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

        if (!checkCroissantPermission(terminal.activity)) {
            output("Croissant app does not seem to have the required permissions.", terminal.theme.warningTextColor)
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

        val newPath = getPathIfExists(terminal.activity, pathN)

        if (newPath != null) {
            terminal.workingDir = newPath
            terminal.setPromptText()
            return
        }

        output(terminal.activity.getString(R.string.error_no_matching_directory_found), terminal.theme.errorTextColor)
        return
    }
}
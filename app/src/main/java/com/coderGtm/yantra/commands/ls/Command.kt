package com.coderGtm.yantra.commands.ls

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.croissant.Croissant
import com.coderGtm.yantra.isCroissantInstalled
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "ls",
        helpTitle = "ls [-a]",
        description = terminal.activity.getString(R.string.cmd_ls_help)
    )

    override fun execute(command: String) {
        if (!isCroissantInstalled(terminal)) {
            val releasePageUrl = "https://github.com/Anready/Croissant/releases"
            val appName = "Croissant"
            output(
                terminal.activity.getString(R.string.this_cmd_handled_via_third_party_app, appName, releasePageUrl), terminal.theme.warningTextColor, null, true)
            return
        }

        if (!Croissant().checkCroissantPermission(terminal.activity)) {
            val appName = "Croissant"
            output(terminal.activity.getString(R.string.app_does_not_have_reqd_perms, appName), terminal.theme.warningTextColor)
            return
        }

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

        val files = Croissant().getListOfObjects(terminal, terminal.workingDir)

        if (files.isEmpty()) {
            return
        }

        for (obj in files) {
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
}
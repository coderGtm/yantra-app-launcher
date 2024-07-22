package com.coderGtm.yantra.commands.backup

import android.content.Intent
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.isPro
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "backup",
        helpTitle = terminal.activity.getString(R.string.cmd_backup_title),
        description = terminal.activity.getString(R.string.cmd_backup_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.size > 1) {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
            return
        }

        if (args.isNotEmpty() && args[0] == "-i") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }

            val mainAct = terminal.activity as MainActivity
            mainAct.selectFileLauncher.launch(Intent.createChooser(intent,
                terminal.activity.getString(R.string.select_backup_file)))

            return
        }

        if (args.isNotEmpty() && args[0] == "-t") {
            if (!isPro(terminal.activity)) {
                output("Sorry, but import of themes is only in Pro version", terminal.theme.errorTextColor)
                return
            }

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }

            val mainAct = terminal.activity as MainActivity
            mainAct.selectThemeLauncher.launch(Intent.createChooser(intent,
                "Select theme"))

            return
        }

        val fileName = packFile(this@Command)


        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        val mainAct = terminal.activity as MainActivity
        mainAct.sendFileLauncher.launch(Intent.createChooser(intent,
            terminal.activity.getString(R.string.save_backup_file)))

        return
    }
}
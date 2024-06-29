package com.coderGtm.yantra.commands.backup

import android.content.Intent
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.io.File

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "backup",
        helpTitle = terminal.activity.getString(R.string.backup_title),
        description = terminal.activity.getString(R.string.backup_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.size > 1) {
            output(terminal.activity.getString(R.string.backup_too_many_arguments), terminal.theme.errorTextColor)
            return
        }

        if (args.isNotEmpty() && args[0] == "-i") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }

            val mainAct = terminal.activity as MainActivity
            mainAct.selectFileLauncher.launch(Intent.createChooser(intent, "Select file"))

            output(terminal.activity.getString(R.string.import_dialog), terminal.theme.successTextColor)
            return
        }

        output(terminal.activity.getString(R.string.exporting), terminal.theme.successTextColor)

        packFile(this@Command)
        val filePath = "YantraBackup.zip"
        val newName = "YantraBackup.yle"

        val oldFile = File(terminal.activity.filesDir, filePath)
        renameFile(oldFile, newName)

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, newName)
        }

        val mainAct = terminal.activity as MainActivity
        mainAct.sendFileLauncher.launch(Intent.createChooser(intent, "Send file"))

        output(terminal.activity.getString(R.string.send_dialog), terminal.theme.successTextColor)
        return
    }
}
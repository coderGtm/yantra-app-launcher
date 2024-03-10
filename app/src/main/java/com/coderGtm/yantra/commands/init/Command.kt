package com.coderGtm.yantra.commands.init

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.getInit
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "init",
        helpTitle = "init",
        description = terminal.activity.getString(R.string.cmd_init_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmd_takes_no_params, metadata.name), terminal.theme.errorTextColor)
            return
        }
        output(terminal.activity.getString(R.string.opening_init_tasks))
        val initListString = getInit(terminal.preferenceObject)
        val initDialog = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
            .setTitle(terminal.activity.getString(R.string.initialization_tasks))
            .setMessage(terminal.activity.getString(R.string.init_disclaimer))
            .setView(R.layout.dialog_multiline_input)
            .setCancelable(false)
            .setPositiveButton(terminal.activity.getString(R.string.save)) { dialog, _ ->
                val initTextBody = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
                val initListBody = initTextBody.trim()
                terminal.preferenceObject.edit().putString("initList",initListBody).apply()
                output(terminal.activity.getString(R.string.init_list_saved),terminal.theme.successTextColor)
            }
            .setNegativeButton(terminal.activity.getString(R.string.clear)) { _, _ ->
                terminal.preferenceObject.edit().putString("initList","").apply()
                output(terminal.activity.getString(R.string.init_list_cleared),terminal.theme.successTextColor)
            }
            .setNeutralButton(terminal.activity.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        initDialog.findViewById<EditText>(R.id.bodyText)?.setText(initListString)
    }
}
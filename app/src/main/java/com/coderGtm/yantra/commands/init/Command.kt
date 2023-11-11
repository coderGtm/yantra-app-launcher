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
        description = "A special script (function) to execute specified commands automatically whenever Launcher is opened or navigated to."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'init' command does not take any arguments.", terminal.theme.errorTextColor)
            return
        }
        output("Opening Initialization Tasks for Yantra Launcher")
        val initListString = getInit(terminal.preferenceObject)
        val initDialog = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
            .setTitle("Initialization Tasks")
            .setMessage("Enter commands one-per-line to execute when Yantra Launcher gets in focus (opened or navigated-back to).")
            .setView(R.layout.dialog_multiline_input)
            .setCancelable(false)
            .setPositiveButton("Save") { dialog, _ ->
                val initTextBody = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
                val initListBody = initTextBody.trim()
                terminal.preferenceObject.edit().putString("initList",initListBody).apply()
                output("Init List saved Successfully",terminal.theme.successTextColor)
            }
            .setNegativeButton("Clear") { _, _ ->
                terminal.preferenceObject.edit().putString("initList","").apply()
                output("Init List cleared",terminal.theme.successTextColor)
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        initDialog.findViewById<EditText>(R.id.bodyText)?.setText(initListString)
    }
}
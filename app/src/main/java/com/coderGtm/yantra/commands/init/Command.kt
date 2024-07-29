package com.coderGtm.yantra.commands.init

import android.text.InputType
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.getInit
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

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
        YantraLauncherDialog(terminal.activity).takeInput(
            title = terminal.activity.getString(R.string.initialization_tasks),
            message = terminal.activity.getString(R.string.init_disclaimer),
            initialInput = initListString,
            cancellable = false,
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE,
            positiveButton = terminal.activity.getString(R.string.save),
            negativeButton = terminal.activity.getString(R.string.clear),
            positiveAction = {
                val initListBody = it.trim()
                terminal.preferenceObject.edit().putString("initList",initListBody).apply()
                output(terminal.activity.getString(R.string.init_list_saved),terminal.theme.successTextColor)
            },
            negativeAction = {
                YantraLauncherDialog(terminal.activity).showInfo(
                    title = terminal.activity.getString(R.string.clear_init_list),
                    message = terminal.activity.getString(R.string.clear_init_list_confirmation),
                    cancellable = false,
                    positiveButton = terminal.activity.getString(R.string.clear),
                    negativeButton = terminal.activity.getString(R.string.cancel),
                    positiveAction = {
                        terminal.preferenceObject.edit().putString("initList","").apply()
                        output(terminal.activity.getString(R.string.init_list_cleared),terminal.theme.successTextColor)
                    }
                )
            }
        )
    }
}
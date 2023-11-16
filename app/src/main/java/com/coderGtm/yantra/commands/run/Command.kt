package com.coderGtm.yantra.commands.run

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "run",
        helpTitle = "run script_name",
        description = "Used to run your custom scripts."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify a script name to run.", terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output("'run' command takes only 1 parameter: The script name to run.", terminal.theme.errorTextColor)
            return
        }
        val rcvdScriptName = args[1]
        val scripts = getScripts(terminal.preferenceObject)

        if (rcvdScriptName in scripts) {
            val scriptBody = terminal.preferenceObject.getString("script_$rcvdScriptName","") ?: ""
            val cmdsInScript = scriptBody.split("\n")
            cmdsInScript.forEach {
                terminal.handleCommand(it.trim())
            }
        }
        else {
            output("Script '$rcvdScriptName' is not defined. Use 'scripts' command to create your own Yantra Scripts.",terminal.theme.errorTextColor)
            return
        }
    }
}
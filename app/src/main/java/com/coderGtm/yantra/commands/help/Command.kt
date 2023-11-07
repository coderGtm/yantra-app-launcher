package com.coderGtm.yantra.commands.help

import android.content.Context
import android.graphics.Typeface
import android.hardware.camera2.CameraManager
import android.os.Build
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import kotlin.reflect.KProperty

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "help",
        helpTitle = "help | help [command_name]",
        description = "Documentation for all commands of Yantra Launcher. Use 'help cmd_name' to get documentation for specific command."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        when (args.size) {
            1 -> {
                output("---Yantra Launcher Help---",terminal.theme.successTextColor, Typeface.BOLD_ITALIC)
                output("-------------------------",terminal.theme.resultTextColor)
                for (commandClass in terminal.commands) {
                    val kClass = commandClass.javaClass.kotlin
                    val metadataProperty = kClass.members.find { it.name == "metadata" }
                    if (metadataProperty is KProperty<*>) {
                        val metadataValue = metadataProperty.call(kClass.objectInstance)
                        if (metadataValue is CommandMetadata) {
                            output(metadata.helpTitle ,terminal.theme.warningTextColor, Typeface.BOLD)
                            output(metadata.description ,terminal.theme.resultTextColor)
                            output("-------------------------",4)
                        }
                    }
                }
                output("-------------------------",terminal.theme.resultTextColor)
                output("Enjoy ㄟ( ▔, ▔ )ㄏ",terminal.theme.successTextColor, Typeface.BOLD_ITALIC)
            }
            2 -> {
                val cmd = args[1].trim().lowercase()
                for (commandClass in terminal.commands) {
                    val kClass = commandClass.javaClass.kotlin
                    val metadataProperty = kClass.members.find { it.name == "metadata" }
                    if (metadataProperty is KProperty<*>) {
                        val metadataValue = metadataProperty.call(kClass.objectInstance)
                        if (metadataValue is CommandMetadata) {
                            if (metadata.name == cmd) {
                                output(metadata.helpTitle ,terminal.theme.warningTextColor, Typeface.BOLD)
                                output(metadata.description ,terminal.theme.resultTextColor)
                                output("-------------------------",4)
                                return
                            }
                        }
                    }
                }
                output("Command not found. Use 'help' to get list of available commands.", terminal.theme.errorTextColor)
            }
            else -> {
                output("Invalid command usage. See 'help' for usage info", terminal.theme.errorTextColor)
            }
        }
    }
}
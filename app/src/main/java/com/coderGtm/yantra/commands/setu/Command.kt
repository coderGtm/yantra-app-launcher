package com.coderGtm.yantra.commands.setu

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "setu",
        helpTitle = "setu [new][name]",
        description = "Setu is used to form Custom Bridges between Yantra Launcher and internals of all other apps. Using 'setu' you can directly launch specific screens or actions of other apps without navigating all the way to it from the start. Use 'setu' to list all setus. Use 'setu new' to create a new Setu (Bridge). Use 'setu setu_name' to open a Setu for editing or deleting. Also, 'setu -1' destroys all your setus."
    )

    override fun execute(command: String) {
        val args = command.split(" ")

    }
}
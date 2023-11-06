package com.coderGtm.yantra.blueprints

import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

abstract class BaseCommand(val terminal: Terminal) {

    abstract val metadata: CommandMetadata
    abstract fun execute(command: String)

    fun output(text: String, state: Int, style: Int? = null) {
        terminal.output(text = text, color = state, style = style)
    }
}
package com.coderGtm.yantra.blueprints

import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

abstract class BaseCommand(val terminal: Terminal) {

    abstract val metadata: CommandMetadata
    abstract fun execute(command: String)

    fun output(text: String, state: Int = terminal.theme.resultTextColor, style: Int? = null, markdown: Boolean = false) {
        terminal.output(text = text, color = state, style = style, markdown = markdown)
    }
}
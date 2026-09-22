package com.coderGtm.yantra.cli.shell

import com.coderGtm.yantra.core.shell.Shell
import com.coderGtm.yantra.terminal.Terminal

class TerminalShell(
    private val terminal: Terminal
) : Shell {

    override fun initialize() {
        terminal.initialize()
    }

    override fun resume() {
        terminal.binding.requestCommandInputFocus()
    }

    override fun pause() {
        // Compose handles visibility via activeShellName state
    }

    override fun destroy() {
        // Compose handles visibility via activeShellName state
    }

    override fun onBackPressed(): Boolean {
        terminal.binding.cmdInput.setText("")
        return true
    }

    fun getTerminal(): Terminal {
        return terminal
    }
}
package com.coderGtm.yantra.core.shell

class ShellManager {

    var onShellChanged: ((String, Shell) -> Unit)? = null

    private var currentShell: Shell? = null

    private var currentShellName = "cli"

    private val initializedShells = mutableSetOf<Shell>()

    fun switchShell(
        name: String,
        shell: Shell
    ) {

        if (currentShell === shell) {
            return
        }

        currentShell?.pause()

        currentShell = shell
        currentShellName = name

        if (!initializedShells.contains(shell)) {

            shell.initialize()

            initializedShells.add(shell)

        }

        shell.resume()

        onShellChanged?.invoke(name, shell)

    }

    fun currentShell(): Shell? = currentShell

    fun currentShellName(): String? = currentShellName

    fun getCurrentShellName(): String = currentShellName
}
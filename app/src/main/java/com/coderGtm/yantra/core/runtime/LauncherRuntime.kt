package com.coderGtm.yantra.core.runtime

import com.coderGtm.yantra.core.shell.Shell
import com.coderGtm.yantra.core.shell.ShellManager

object LauncherRuntime {

    lateinit var shellManager: ShellManager

    private val shells = mutableMapOf<String, Shell>()

    var currentShell: Shell? = null
        private set

    fun registerShell(
        name: String,
        shell: Shell
    ) {
        shells[name.lowercase()] = shell
    }

    fun getShell(name: String): Shell? {
        return shells[name.lowercase()]
    }

    fun hasShell(name: String): Boolean {
        return shells.containsKey(name.lowercase())
    }

    fun unregisterShell(name: String): Boolean {
        if (name.equals("cli", true) || name.equals("gui", true)) return false
        val normalized = name.lowercase()
        val removed = shells.remove(normalized) != null
        if (removed && shellManager.getCurrentShellName().equals(normalized, true)) {
            shellManager.switchShell("cli", shells["cli"] ?: return removed)
            currentShell = shells["cli"]
        }
        return removed
    }

    fun listShells(): List<String> =
        shells.keys.sorted()

    fun switchShell(name: String): Boolean {

        val shell = getShell(name) ?: return false

        shellManager.switchShell(
            name,
            shell
        )

        currentShell = shell

        return true
    }


}
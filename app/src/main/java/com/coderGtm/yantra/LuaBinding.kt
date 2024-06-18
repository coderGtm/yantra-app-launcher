package com.coderGtm.yantra

import com.coderGtm.yantra.terminal.Terminal
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction

class LuaBinding(private val terminal: Terminal) : LuaTable() {

    init {
        set("exec", ExecFunction())
    }

    private inner class ExecFunction : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            val command = when {
                arg.isstring() -> arg.tojstring()
                arg.istable() -> {
                    // Handle table case if needed
                    // Example: extracting string from a table
                    val stringValue = arg.get("cmd")
                    if (stringValue.isstring()) stringValue.tojstring() else ""
                }
                else -> ""
            }
            terminal.handleCommand(command)
            return LuaValue.NIL
        }
    }
}
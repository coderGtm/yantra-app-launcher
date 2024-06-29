package com.coderGtm.yantra

import LuaHttpAPI
import com.coderGtm.yantra.commands.run.requestInput
import com.coderGtm.yantra.terminal.Terminal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.jse.JsePlatform


class LuaExecutor(private val scriptName: String, private val terminal: Terminal) {

    private val globals: Globals = JsePlatform.standardGlobals()
    private var luaThread: Thread? = null
    private val binding: LuaBinding = LuaBinding(terminal)
    private val http = LuaHttpAPI(terminal.activity.baseContext)

    init {
        globals.set("print", PrintFunction())
        globals.set("input", InputFunction())
        globals.set("http", http)
        globals.set("binding", binding)
    }

    fun execute(luaCode: String) {
        luaThread = Thread {
            try {
                val chunk: LuaValue = globals.load(luaCode)
                chunk.call()
            } catch (e: LuaError) {
                terminal.output("Error executing Lua code: ${e.message}", terminal.theme.errorTextColor, null)
            } catch (e: Exception) {
                terminal.output("Unexpected error: ${e.message}", terminal.theme.errorTextColor, null)
            }
        }
        luaThread?.start()
    }

    fun terminate() {
        luaThread?.interrupt()
    }

    private inner class PrintFunction : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            terminal.output(arg.tojstring(), terminal.theme.resultTextColor, null)
            return LuaValue.NIL
        }
    }

    private inner class InputFunction : OneArgFunction() {
        override fun call(prompt: LuaValue): LuaValue {
            val deferred = CompletableDeferred<String>()
            val promptString = prompt.tojstring()
            terminal.activity.runOnUiThread {
                requestInput(this@LuaExecutor, terminal, scriptName) { input ->
                    deferred.complete(input)
                }
            }
            return runBlocking {
                val input = deferred.await()
                LuaValue.valueOf(input)
            }
        }
    }
}
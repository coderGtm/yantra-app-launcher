package com.coderGtm.yantra.blueprints

import com.coderGtm.yantra.commands.run.requestInput
import com.coderGtm.yantra.terminal.Terminal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.DebugLib
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import kotlin.coroutines.cancellation.CancellationException


class LuaExecutor(private val scriptName: String, private val terminal: Terminal) {

    private val globals: Globals = JsePlatform.standardGlobals()
    private var luaThread: Thread? = null

    init {
        globals.set("print", PrintFunction())
        globals.set("input", InputFunction())
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
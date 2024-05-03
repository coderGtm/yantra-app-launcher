package com.coderGtm.yantra.commands.run

import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "run",
        helpTitle = terminal.activity.getString(R.string.cmd_run_title),
        description = terminal.activity.getString(R.string.cmd_run_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_script_to_run), terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output(terminal.activity.getString(R.string.run_only_one_param), terminal.theme.errorTextColor)
            return
        }
        val rcvdScriptName = args[1]
        val scripts = getScripts(terminal.preferenceObject)

        if (rcvdScriptName in scripts) {
            val scriptBody = terminal.preferenceObject.getString("script_$rcvdScriptName","") ?: ""
            val pyCommand = """
                def run(name):
                    print("-exccmd-" + name + "-endcmd-")


            """.trimIndent() + scriptBody
            println(scriptBody)

            output(terminal.activity.getString(R.string.cmd_run_start), terminal.theme.resultTextColor)

            GlobalScope.launch {
                if (!Python.isStarted()) {
                    output(terminal.activity.getString(R.string.cmd_run_startPy), terminal.theme.resultTextColor)
                    Python.start(AndroidPlatform(terminal.activity))
                }

                val py = Python.getInstance()

                val sys: PyObject = py.getModule("sys")
                val io: PyObject = py.getModule("io")

                val console: PyObject = py.getModule("interpreter")
                var textOutputStream = io.callAttr("StringIO")
                sys["stdout"] = textOutputStream

                try {
                    var i = true
                    GlobalScope.launch {
                        console.callAttrThrows("mainTextCode", pyCommand)
                        i = false
                    }

                    GlobalScope.launch {
                        var oldString = ""
                        while (i) {
                            var newString = textOutputStream.callAttr("getvalue").toString()
                            if (oldString != newString) {
                                textOutputStream = io.callAttr("StringIO")
                                sys["stdout"] = textOutputStream
                                val indexOfCommand = newString.indexOf("-exccmd-")
                                val indexOfEnd = newString.indexOf("-endcmd-")

                                if (newString.length > 8 && indexOfCommand != -1) {
                                    val cmd = newString.substring(indexOfCommand + 8, indexOfEnd)
                                    val allCmd = newString.substring(indexOfCommand, indexOfEnd + 8)
                                    output(newString.replace(allCmd, ""), terminal.theme.resultTextColor)
                                    terminal.handleCommand(cmd)
                                    oldString = newString
                                    continue
                                }

                                output(newString, terminal.theme.resultTextColor)
                                oldString = newString
                            }
                        }

                        output(terminal.activity.getString(R.string.cmd_run_end), terminal.theme.successTextColor)
                    }
                } catch (e: PyException) {
                    output(e.message.toString(), terminal.theme.errorTextColor)
                } catch (throwable: Throwable) {
                    output(throwable.toString(), terminal.theme.errorTextColor)
                }
            }
        }
        else {
            output(terminal.activity.getString(R.string.script_not_found, rcvdScriptName),terminal.theme.errorTextColor)
            return
        }
    }
}
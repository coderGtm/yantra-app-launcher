package com.coderGtm.yantra.commands.calc

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "calc",
        helpTitle = "calc [expression]",
        description = "An in-built calculator to evaluate arithmetic expressions. It does addition, subtraction, multiplication, division, exponentiation (using the ^ symbol), and a few basic functions like sqrt, sin, cos and tan. It supports grouping using (...), and it gets the operator precedence and associativity rules correct.\n\nExample: 'calc ((4 - 2^3 + 1) * -sqrt(3*3+4*4)) / 2' gives 7.5"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify an expression to evaluate.", terminal.theme.errorTextColor)
            return
        }
        val helper = CommandHelper()
        val expression = command.removePrefix(args[0]).trim()
        try {
            val result = helper.eval(expression)
            output(result.toString())
        }
        catch (e: RuntimeException) {
            output(e.message.toString(), terminal.theme.errorTextColor)
        }
    }
}
package com.coderGtm.yantra.commands.calc

import android.content.Context
import com.coderGtm.yantra.R

fun eval(str: String, context: Context): Double {
    return object : Any() {
        var pos = -1
        var ch = 0
        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException(context.getString(R.string.unexpected_char) + ch.toChar())
            return x
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number
        //        | functionName `(` expression `)` | functionName factor
        //        | factor `^` factor
        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) x /= parseFactor() // division
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus
            var x: Double
            val startPos = pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                if (!eat(')'.code)) throw RuntimeException(context.getString(R.string.missing_parenthesis))
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, pos)
                if (eat('('.code)) {
                    x = parseExpression()
                    if (!eat(')'.code)) throw RuntimeException(context.getString(R.string.missing_parenthesis_after_arg, func))
                } else {
                    x = parseFactor()
                }
                x =
                    if (func == "sqrt") Math.sqrt(x) else if (func == "sin") Math.sin(
                        Math.toRadians(
                            x
                        )
                    ) else if (func == "cos") Math.cos(
                        Math.toRadians(x)
                    ) else if (func == "tan") Math.tan(Math.toRadians(x)) else throw RuntimeException(
                        context.getString(R.string.clac_unknown_function, func)
                    )
            } else {
                throw RuntimeException(context.getString(R.string.unexpected_char)  + ch.toChar())
            }
            if (eat('^'.code)) x = Math.pow(x, parseFactor()) // exponentiation
            return x
        }
    }.parse()
}
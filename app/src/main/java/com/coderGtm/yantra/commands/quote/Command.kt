package com.coderGtm.yantra.commands.quote

import android.graphics.Typeface
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.io.InputStream
import java.util.Random

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "quote",
        helpTitle = "quote",
        description = "Displays a random quote! What else do you expect?"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'quote' command does not take any parameters", terminal.theme.errorTextColor)
            return
        }
        //read quotes from file
        val random = Random()
        val inputStream: InputStream = terminal.activity.assets.open("quotes.txt")
        inputStream.bufferedReader().useLines { lines ->
            val quoteLine = lines.toList()[random.nextInt(1643)]  //1643 is the number of lines in the file
            val quote = quoteLine.split("%-%")[0]
            val author = quoteLine.split("%-%")[1]
            output(quote, terminal.theme.resultTextColor, Typeface.ITALIC)
            output("      ~$author", terminal.theme.resultTextColor, Typeface.ITALIC)
        }
    }

}
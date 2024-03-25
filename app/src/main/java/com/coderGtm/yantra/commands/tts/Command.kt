package com.coderGtm.yantra.commands.tts

import android.graphics.Typeface
import android.speech.tts.TextToSpeech
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "tts",
        helpTitle = terminal.activity.getString(R.string.cmd_tts_title),
        description = terminal.activity.getString(R.string.cmd_tts_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        val text = command.trim().removePrefix(args[0])
        output(terminal.activity.getString(R.string.initializing_tts_engine), terminal.theme.resultTextColor, Typeface.ITALIC)
        val mainAct = terminal.activity as MainActivity
        mainAct.ttsTxt = text.trim()
        mainAct.tts = TextToSpeech(mainAct.baseContext, mainAct)
    }
}
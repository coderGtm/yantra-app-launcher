package com.coderGtm.yantra.commands.sfx

import android.media.MediaPlayer
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "sfx",
        helpTitle = "sfx",
        description = "Play sound effects"
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size == 1) {
            // play default sound
            val mediaPlayer = MediaPlayer.create(terminal.activity, R.raw.beep)
            mediaPlayer.start()
        }
    }
}
package com.coderGtm.yantra.commands.music

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "music",
        helpTitle = "music [-state]",
        description = "Control your music state in your device"
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.size > 1) {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
            return
        }

        if (args.isEmpty()) {
            output("No parameters provided, change state of music", terminal.theme.warningTextColor)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }

        if (args[0] == "play") {
            output("Resuming music", terminal.theme.successTextColor)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }
        if (args[0] == "pause") {
            output("Pausing music", terminal.theme.successTextColor)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PAUSE, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }
        if (args[0] == "next") {
            output("Next music", terminal.theme.successTextColor)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_NEXT, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }

        if (args[0] == "prev") {
            output("Previous music", terminal.theme.successTextColor)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }

        output("Invalid parameter!", terminal.theme.errorTextColor)
        return
    }
}
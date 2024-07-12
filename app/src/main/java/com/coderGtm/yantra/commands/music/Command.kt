package com.coderGtm.yantra.commands.music

import android.content.Context
import android.graphics.Typeface
import android.media.AudioManager
import android.view.KeyEvent
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "music",
        helpTitle = terminal.activity.getString(R.string.cmd_music_title),
        description = terminal.activity.getString(R.string.cmd_music_help),
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.size > 1) {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
            return
        }

        if (args.isEmpty()) {
            output(terminal.activity.getString(R.string.changing_music_state), terminal.theme.successTextColor, Typeface.ITALIC)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }

        if (args[0] == "play") {
            output(terminal.activity.getString(R.string.resuming_music), terminal.theme.successTextColor, Typeface.ITALIC)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }
        if (args[0] == "pause") {
            output(terminal.activity.getString(R.string.pausing_music), terminal.theme.successTextColor, Typeface.ITALIC)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PAUSE, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }
        if (args[0] == "next") {
            output(terminal.activity.getString(R.string.playing_next_track), terminal.theme.successTextColor, Typeface.ITALIC)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_NEXT, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }

        if (args[0] == "prev") {
            output(terminal.activity.getString(R.string.playing_previous_track), terminal.theme.successTextColor, Typeface.ITALIC)
            sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, terminal.activity.getSystemService(
                Context.AUDIO_SERVICE) as AudioManager
            )
            return
        }

        output(
            terminal.activity.getString(
                R.string.invalid_parameter_see_help_to_see_usage_info,
                metadata.name
            ), terminal.theme.errorTextColor)
        return
    }
}
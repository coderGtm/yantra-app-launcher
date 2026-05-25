package com.coderGtm.yantra.activities.main

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.terminal.Terminal
import java.util.Locale

internal class MainActivitySpeechCoordinator(
    private val activity: MainActivity,
    private val terminalProvider: () -> Terminal,
) : TextToSpeech.OnInitListener {

    override fun onInit(status: Int) {
        val terminal = terminalProvider()
        val tts = activity.tts ?: return

        if (status == TextToSpeech.SUCCESS) {
            val languageStatus = tts.setLanguage(Locale.getDefault())
            if (languageStatus == TextToSpeech.LANG_MISSING_DATA || languageStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
                terminal.output(activity.getString(R.string.error_tts_language_not_supported), terminal.theme.errorTextColor, null)
            } else {
                tts.setSpeechRate(.7f)
                tts.speak(activity.ttsTxt, TextToSpeech.QUEUE_FLUSH, null, "")
            }
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                terminal.output(activity.getString(R.string.tts_synthesized_playing_now), terminal.theme.successTextColor, null)
            }

            override fun onDone(utteranceId: String) {
                terminal.output(activity.getString(R.string.shutting_down_tts_engine), terminal.theme.resultTextColor, null)
                shutdownTts(terminal)
            }

            @Deprecated("Deprecated TTS callback signature")
            override fun onError(utteranceId: String) {
                terminal.output(activity.getString(R.string.tts_error), terminal.theme.errorTextColor, null)
                terminal.output(activity.getString(R.string.shutting_down_tts_engine), terminal.theme.resultTextColor, null)
                shutdownTts(terminal)
            }
        })
    }

    private fun shutdownTts(terminal: Terminal) {
        activity.tts?.run {
            stop()
            shutdown()
        }
        terminal.output(activity.getString(R.string.tts_engine_shutdown), terminal.theme.resultTextColor, null)
    }
}

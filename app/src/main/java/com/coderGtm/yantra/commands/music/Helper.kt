package com.coderGtm.yantra.commands.music

import android.media.AudioManager
import android.view.KeyEvent

fun sendMediaButtonEvent(keyCode: Int, audioManager: AudioManager) {
    val eventTime = System.currentTimeMillis()
    val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
    val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)

    audioManager.dispatchMediaKeyEvent(downEvent)
    audioManager.dispatchMediaKeyEvent(upEvent)
}
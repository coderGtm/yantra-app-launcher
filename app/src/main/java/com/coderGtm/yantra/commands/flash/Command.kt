package com.coderGtm.yantra.commands.flash

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "flash",
        description = "Controls Flash"
    )

    override fun execute(flags: Map<String, String>, body: String) {
        if (flags.isNotEmpty()) {
            output("'flash' command does not take any flags!", terminal.theme.errorTextColor)
        }
        if (body.isEmpty()) {
            output("Please specify the state (on/off/0/1)", terminal.theme.errorTextColor)
            return
        }
        val state: Boolean = when (body.lowercase()) {
            "on", "1" -> {
                true
            }
            "off", "0" -> {
                false
            }
            else -> {
                output("Toggle state not recognized. Try using 'on' | 'off' or 0 | 1.", terminal.theme.warningTextColor)
                return
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraM = terminal.activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraListId = cameraM.cameraIdList[0]
            if (state) {
                cameraM.setTorchMode(cameraListId, true)
                output("Flashlight turned on", terminal.theme.successTextColor)
            }
            else {
                cameraM.setTorchMode(cameraListId, false)
                output("Flashlight turned off", terminal.theme.successTextColor)
            }
        }
        else {
            output("Flashlight not supported on this device", terminal.theme.warningTextColor)
        }
    }
}
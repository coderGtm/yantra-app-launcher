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
        helpTitle = "flash [state]",
        description = "Toggles flashlight on/off. Example: 'flash on' or 'flash 0'. Use without any args to toggle the state."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            // get current state
            val cameraM = terminal.activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraListId = cameraM.cameraIdList[0]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraM.registerTorchCallback(object : CameraManager.TorchCallback() {
                    // toggle state
                    override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                        if (enabled) {
                            cameraM.unregisterTorchCallback(this)
                            cameraM.setTorchMode(cameraListId, false)
                            output("Flashlight turned off", terminal.theme.successTextColor)
                        } else {
                            cameraM.unregisterTorchCallback(this)
                            cameraM.setTorchMode(cameraListId, true)
                            output("Flashlight turned on", terminal.theme.successTextColor)
                        }
                    }
                }, null)
            }
            else {
                output("Flashlight not supported on this device", terminal.theme.warningTextColor)
            }
        }
        else if (args.size == 2) {
            val stateInput = args[1]
            val state: Boolean = when (stateInput.lowercase()) {
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
        else {
            output("'flash' command takes only one argument.", terminal.theme.errorTextColor)
        }
    }
}
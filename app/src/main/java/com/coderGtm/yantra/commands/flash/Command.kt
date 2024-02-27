package com.coderGtm.yantra.commands.flash

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "flash",
        helpTitle = terminal.activity.getString(R.string.cmd_flash_title),
        description = terminal.activity.getString(R.string.cmd_flash_help)
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
                            output(terminal.activity.getString(R.string.toggleable_turned_off, metadata.name.replaceFirstChar { it.titlecase() }), terminal.theme.successTextColor)
                        } else {
                            cameraM.unregisterTorchCallback(this)
                            cameraM.setTorchMode(cameraListId, true)
                            output(terminal.activity.getString(R.string.toggleable_turned_on, metadata.name.replaceFirstChar { it.titlecase() }), terminal.theme.successTextColor)
                        }
                    }
                }, null)
            }
            else {
                output(terminal.activity.getString(R.string.cmd_not_supported, metadata.name), terminal.theme.warningTextColor)
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
                    output(terminal.activity.getString(R.string.state_unrecognized), terminal.theme.warningTextColor)
                    return
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cameraM = terminal.activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraListId = cameraM.cameraIdList[0]
                if (state) {
                    cameraM.setTorchMode(cameraListId, true)
                    output(terminal.activity.getString(R.string.toggleable_turned_on, metadata.name.replaceFirstChar { it.titlecase() }), terminal.theme.successTextColor)
                }
                else {
                    cameraM.setTorchMode(cameraListId, false)
                    output(terminal.activity.getString(R.string.toggleable_turned_off, metadata.name.replaceFirstChar { it.titlecase() }), terminal.theme.successTextColor)
                }
            }
            else {
                output(terminal.activity.getString(R.string.cmd_not_supported, metadata.name), terminal.theme.warningTextColor)
            }
        }
        else {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
        }
    }
}
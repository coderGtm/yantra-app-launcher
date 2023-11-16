package com.coderGtm.yantra.commands.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "bluetooth",
        helpTitle = "bluetooth [state]",
        description = "Toggles bluetooth on/off. Example: 'bluetooth on' or 'bt 0'"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify a state for bluetooth", terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output("'bluetooth' command takes only 1 argument", terminal.theme.errorTextColor)
            return
        }
        val stateInput = args[1].trim()
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
        // code for android 12 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothManager: BluetoothManager = terminal.activity.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                toast(terminal.activity.baseContext, "The device doesn't support Bluetooth")
            }
            if (ActivityCompat.checkSelfPermission(terminal.activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                output("Bluetooth permission missing!", terminal.theme.warningTextColor)
                ActivityCompat.requestPermissions(terminal.activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PermissionRequestCodes.BLUETOOTH.code)
                return
            }
            else {
                if (state) {
                    bluetoothAdapter?.enable()
                    output("Bluetooth turned on", terminal.theme.successTextColor)
                }
                else {
                    bluetoothAdapter?.disable()
                    output("Bluetooth turned off", terminal.theme.successTextColor)
                }
            }
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager: BluetoothManager = terminal.activity.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                toast(terminal.activity.baseContext, "The device doesn't support Bluetooth")
            }
            else {

                if (state) {
                    bluetoothAdapter.enable()
                    output("Bluetooth turned on", terminal.theme.successTextColor)
                }
                else {
                    bluetoothAdapter.disable()
                    output("Bluetooth turned off", terminal.theme.successTextColor)
                }
            }
        } else {
            toast(terminal.activity.baseContext, "This feature requires Android 6 or higher")
        }
    }
}
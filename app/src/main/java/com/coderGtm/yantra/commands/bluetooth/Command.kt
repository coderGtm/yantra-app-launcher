package com.coderGtm.yantra.commands.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "bluetooth",
        helpTitle = terminal.activity.getString(R.string.cmd_bluetooth_title),
        description = terminal.activity.getString(R.string.cmd_bluetooth_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_bt_state), terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
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
                output(terminal.activity.getString(R.string.state_unrecognized), terminal.theme.warningTextColor)
                return
            }
        }
        // code for android 12 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothManager: BluetoothManager = terminal.activity.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                toast(terminal.activity.baseContext, terminal.activity.getString(R.string.cmd_not_supported, metadata.name))
            }
            if (ActivityCompat.checkSelfPermission(terminal.activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                output(terminal.activity.getString(R.string.feature_permission_missing, metadata.name), terminal.theme.warningTextColor)
                ActivityCompat.requestPermissions(terminal.activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PermissionRequestCodes.BLUETOOTH.code)
                return
            }
            else {
                if (state) {
                    bluetoothAdapter?.enable()
                    output(terminal.activity.getString(R.string.toggleable_turned_on), terminal.theme.successTextColor)
                }
                else {
                    bluetoothAdapter?.disable()
                    output(terminal.activity.getString(R.string.toggleable_turned_off, metadata.name.replaceFirstChar { it.titlecase() }), terminal.theme.successTextColor)
                }
            }
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager: BluetoothManager = terminal.activity.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                toast(terminal.activity.baseContext, terminal.activity.getString(R.string.cmd_not_supported, metadata.name))
            }
            else {

                if (state) {
                    bluetoothAdapter.enable()
                    output(terminal.activity.getString(R.string.toggleable_turned_on, metadata.name.replaceFirstChar { it.titlecase() }), terminal.theme.successTextColor)
                }
                else {
                    bluetoothAdapter.disable()
                    output(terminal.activity.getString(R.string.toggleable_turned_off, metadata.name.replaceFirstChar { it.titlecase() }), terminal.theme.successTextColor)
                }
            }
        } else {
            toast(terminal.activity.baseContext, terminal.activity.getString(R.string.min_android6_reqd))
        }
    }
}
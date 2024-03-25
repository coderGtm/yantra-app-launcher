package com.coderGtm.yantra.commands.battery

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import kotlin.math.roundToInt

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "battery",
        helpTitle = "battery",
        description = terminal.activity.getString(R.string.cmd_battery_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            terminal.activity.baseContext.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Float = level / scale.toFloat()
        val charging: Boolean = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
        if (args.isEmpty()) {
            val health: String = when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                BatteryManager.BATTERY_HEALTH_COLD -> terminal.activity.getString(R.string.cold)
                BatteryManager.BATTERY_HEALTH_DEAD -> terminal.activity.getString(R.string.dead)
                BatteryManager.BATTERY_HEALTH_GOOD -> terminal.activity.getString(R.string.good)
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> terminal.activity.getString(R.string.over_voltage)
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> terminal.activity.getString(R.string.overheat)
                BatteryManager.BATTERY_HEALTH_UNKNOWN -> terminal.activity.getString(R.string.unknown)
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> terminal.activity.getString(R.string.unspecified_failure)
                else -> terminal.activity.getString(R.string.unknown)
            }
            val temperature: Float = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.div(10f) ?: -1f
            val temperatureFahrenheit = String.format("%.1f", (temperature * 1.8f) + 32)
            output(terminal.activity.getString(R.string.battery_status), terminal.theme.successTextColor)
            output("-------------------------")
            output(terminal.activity.getString(R.string.level, (batteryPct*100).toInt()))
            output(terminal.activity.getString(R.string.charging, charging))
            output(terminal.activity.getString(R.string.health, health))
            output(terminal.activity.getString(R.string.temperature_c_f, temperature.toString(), temperatureFahrenheit))
            output("-------------------------")
        }
        else if (args.size == 1 && args.first() == "-bar") {
            // show battery bar
            val totalBars = 10
            val filledBars = (batteryPct * totalBars).roundToInt()
            val emptyBars = totalBars - filledBars
            val filledBarSymbol = "#"
            val emptyBarSymbol = "."
            val chargingPrefix = if (charging) "⚡" else ""

            val barString = "$chargingPrefix[" + filledBarSymbol.repeat(filledBars) + "${(batteryPct * 100).toInt()}%" + emptyBarSymbol.repeat(emptyBars) + "]"
            output(barString)
        }
        else {
            output(terminal.activity.getString(R.string.battery_invalid_Args),terminal.theme.errorTextColor)
        }
    }
}
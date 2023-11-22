package com.coderGtm.yantra.commands.battery

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import kotlin.math.roundToInt

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "battery",
        helpTitle = "battery",
        description = "Shows current Battery Level. Use the optional '-bar' argument to show just the battery percentage and charging status in visual form."
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
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
                else -> "Unknown"
            }
            val temperature: Float = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.div(10f) ?: -1f
            val temperatureFahrenheit = String.format("%.1f", (temperature * 1.8f) + 32)
            output("Battery Status:", terminal.theme.successTextColor)
            output("-------------------------")
            output("-> Level: ${(batteryPct*100).toInt()}%")
            output("-> Charging: $charging")
            output("-> Health: $health")
            output("-> Temperature: $temperature°C ($temperatureFahrenheit°F)")
            output("-------------------------")
        }
        else if (args.size == 1 && args.first() == "-bar") {
            // show battery bar
            val totalBars = 10
            val filledBars = (batteryPct * totalBars).roundToInt()
            val emptyBars = totalBars - filledBars
            val filledBarSymbol = "|"
            val emptyBarSymbol = "x"
            val chargingPrefix = if (charging) "⚡" else ""

            val barString = "$chargingPrefix[" + filledBarSymbol.repeat(filledBars) + "${(batteryPct * 100).toInt()}%" + emptyBarSymbol.repeat(emptyBars) + "]"
            output(barString)
        }
        else {
            output("Invalid args provided. See \"help battery\" for usage info",terminal.theme.errorTextColor)
        }
    }
}
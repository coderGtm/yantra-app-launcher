package com.coderGtm.yantra.commands.sysinfo

import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.os.SystemClock
import com.coderGtm.yantra.BuildConfig
import com.coderGtm.yantra.DEFAULT_TERMINAL_FONT_NAME
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.getUserName
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "sysinfo",
        helpTitle = terminal.activity.getString(R.string.cmd_sysinfo_title),
        description = terminal.activity.getString(R.string.cmd_sysinfo_help)
    )

    override fun execute(command: String) {
        var args = command.split(" ").drop(1)
        val actManager = terminal.activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val availableMem = memInfo.availMem.toDouble() / (1024*1024) // Megabytes
        val totalMem = memInfo.totalMem.toDouble() / (1024*1024) // Megabytes

        val uptimeHours = SystemClock.uptimeMillis() / (1000*60*60) // Hours
        val uptimeMinutes = SystemClock.uptimeMillis() / (1000*60*60*60) // Minutes

        val widthRes = terminal.activity.windowManager.defaultDisplay.width
        val heightRes = terminal.activity.windowManager.defaultDisplay.height

        val showAllInfo = args.isEmpty()

        if (showAllInfo) {
            output("${getUserName(terminal.preferenceObject)}@YantraLauncher", terminal.theme.warningTextColor)
            output("-------------------------", terminal.theme.warningTextColor)
            args = listOf("-os","-host","-kernel","-uptime","-apps","-terminal","-font","-resolution","-theme","-cpu","-memory")
        }
        for (arg in args) {
            when (arg.lowercase()) {
                "-os" -> output("--> OS: Android ${Build.VERSION.RELEASE}")
                "-host" -> output("--> Host: ${Build.MANUFACTURER} ${Build.MODEL}")
                "-kernel" -> output("--> Kernel: ${System.getProperty("os.version")}")
                "-uptime" -> output("--> Uptime: ${uptimeHours}h ${uptimeMinutes}m")
                "-apps" -> output("--> Apps: ${terminal.appList.size}")
                "-terminal" -> output("--> Terminal: Yantra Launcher ${BuildConfig.VERSION_NAME}")
                "-font" -> output("--> Terminal Font: ${terminal.preferenceObject.getString("font", DEFAULT_TERMINAL_FONT_NAME) ?: DEFAULT_TERMINAL_FONT_NAME}")
                "-resolution" -> output("--> Resolution: ${widthRes}x${heightRes}")
                "-theme" -> output("--> Theme: ${getCurrentThemeName(terminal.preferenceObject)}")
                "-cpu" -> output("--> CPU: ${Build.SUPPORTED_ABIS[0]} (${Runtime.getRuntime().availableProcessors()}) @ ${getCPUSpeed()}")
                "-memory" -> output("--> Memory: ${availableMem.toInt()}MiB / ${totalMem.toInt()}MiB")
                else -> output(terminal.activity.getString(R.string.unknown_flag, arg), terminal.theme.errorTextColor)
            }
        }
        if (showAllInfo) {
            output("-------------------------", terminal.theme.warningTextColor)
        }
    }
}
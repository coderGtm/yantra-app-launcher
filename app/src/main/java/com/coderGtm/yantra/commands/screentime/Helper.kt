package com.coderGtm.yantra.commands.screentime

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.coderGtm.yantra.R
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.terminal.Terminal
import java.util.concurrent.TimeUnit


@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
fun getScreenTime(terminal: Terminal, startTime: Long, endTime: Long, appList: ArrayList<AppBlock>): Map<String, Int?> {
    var currentEvent: UsageEvents.Event
    val allEvents: MutableList<UsageEvents.Event> = ArrayList()
    val appUsageMap = HashMap<String, Int?>()

    val usageEvents =
        (terminal.activity.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager)
            .queryEvents(startTime, endTime)

    while (usageEvents.hasNextEvent()) {
        currentEvent = UsageEvents.Event()
        usageEvents.getNextEvent(currentEvent)
        if ((currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) && currentEvent.packageName in appList.map { it.packageName }) {
            allEvents.add(currentEvent)
            val key = currentEvent.packageName
            if (appUsageMap[key] == null) appUsageMap[key] = 0
        }
    }

    try {
        for (i in 0 until allEvents.size - 1) {
            val e0 = allEvents[i]
            val e1 = allEvents[i + 1]
            if (e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED && e1.eventType == UsageEvents.Event.ACTIVITY_PAUSED && e0.className == e1.className) {
                val diff = (e1.timeStamp - e0.timeStamp).toInt()
                var prev = appUsageMap[e0.packageName]
                if (prev == null) prev = 0
                appUsageMap[e0.packageName] = prev + diff
            }
        }
        val lastEvent = allEvents[allEvents.size - 1]
        if (lastEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
            val diff = System.currentTimeMillis().toInt() - lastEvent.timeStamp.toInt()
            var prev = appUsageMap[lastEvent.packageName]
            if (prev == null) prev = 0
            appUsageMap[lastEvent.packageName] = prev + diff
        }
        // sort the map by values in descending order
        val sortedMap = appUsageMap.toList().sortedByDescending { (_, value) -> value }.toMap()

        return sortedMap
    }
    catch (e: Exception) {
        return emptyMap()
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
fun getTotalScreenTime(terminal: Terminal, startTime: Long, endTime: Long, appList: ArrayList<AppBlock>): String {
    var totalScreenTime = 0L
    val sortedMap = getScreenTime(terminal, startTime, endTime, appList)
    sortedMap.forEach { (key, value) ->
        // if not in the app list, skip
        val appBlock = appList.find { it.packageName == key } ?: return@forEach
        //terminal.output("$appName: ${formatTime(value!!.toLong())}", terminal.theme.resultTextColor, null)
        totalScreenTime += value!!
    }
    // Format the total time (in milliseconds) into a readable format
    val screenTime = formatScreenTime(totalScreenTime, terminal.activity)
    return screenTime
}

fun formatScreenTime(milliseconds: Long, context: Context): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds - TimeUnit.HOURS.toMillis(hours))
    val seconds =
        TimeUnit.MILLISECONDS.toSeconds(milliseconds - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes))
    // estimate the time
    return if (seconds > 30) {
        if (hours == 0L) {
            "${minutes+1} min"
        }
        else {
            "$hours hr ${minutes+1} min"
        }
    }
    else if (hours == 0L && minutes == 0L) {
        context.getString(R.string.less_than_1_minute)
    }
    else {
        if (hours == 0L) {
            "$minutes min"
        }
        else {
            "$hours hr $minutes min"
        }
    }
}

fun checkUsageStatsPermission(terminal: Terminal) : Boolean {
    val appOpsManager = terminal.activity.getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager
    // `AppOpsManager.checkOpNoThrow` is deprecated from Android Q
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsManager.unsafeCheckOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(), terminal.activity.packageName
        )
    }
    else {
        appOpsManager.checkOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(), terminal.activity.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}
package com.coderGtm.yantra.gui.desktop.repository

import android.content.Context
import com.coderGtm.yantra.SYSTEM_PKG_CLI
import com.coderGtm.yantra.SYSTEM_PKG_SETTINGS
import com.coderGtm.yantra.gui.desktop.model.HomeItem
import com.coderGtm.yantra.gui.desktop.model.HomeLayout
import com.coderGtm.yantra.gui.desktop.model.HomePage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class HomeRepository(private val context: Context) {
    private val layoutFile = File(context.filesDir, "yantra/home_layout.json")
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true 
    }

    fun save(layout: HomeLayout) {
        try {
            layoutFile.parentFile?.mkdirs()
            layoutFile.writeText(json.encodeToString(layout), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load(): HomeLayout {
        if (!layoutFile.exists()) {
            val defaultLayout = createDefaultLayout()
            save(defaultLayout)
            return defaultLayout
        }
        return try {
            json.decodeFromString<HomeLayout>(layoutFile.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            e.printStackTrace()
            HomeLayout()
        }
    }

    private fun createDefaultLayout(): HomeLayout {
        val pm = context.packageManager
        val commonPackages = listOf(
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.messaging",
            "com.google.android.apps.messaging",
            "com.android.chrome",
            "com.google.android.browser",
            "com.android.vending",
            "com.google.android.apps.photos",
            "com.android.gallery3d",
            "com.android.settings",
            "com.google.android.calendar",
            "com.android.calendar"
        )

        val homeItems = mutableListOf<HomeItem>()
        homeItems.add(HomeItem(SYSTEM_PKG_CLI, 0, 0))
        var col = 1
        var row = 0

        commonPackages.forEach { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                homeItems.add(HomeItem(pkg, col, row))
                col++
                if (col >= 4) {
                    col = 0
                    row++
                }
                if (row >= 4) return@forEach // Limit default items
            } catch (e: Exception) {
                // App not installed
            }
        }

        val dockPackages = mutableListOf<String>(
            SYSTEM_PKG_CLI,
            SYSTEM_PKG_SETTINGS
        )
        val commonDock = listOf(
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.messaging",
            "com.google.android.apps.messaging",
            "com.android.chrome",
            "com.google.android.browser"
        )

        commonDock.forEach { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                if (dockPackages.size < 4) {
                    dockPackages.add(pkg)
                }
            } catch (e: Exception) {}
        }

        return HomeLayout(
            pages = listOf(HomePage(0, homeItems)),
            dockPackages = dockPackages
        )
    }
}

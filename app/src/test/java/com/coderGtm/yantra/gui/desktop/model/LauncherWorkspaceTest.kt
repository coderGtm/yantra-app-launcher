package com.coderGtm.yantra.gui.desktop.model

import com.coderGtm.yantra.SYSTEM_PKG_CLI
import com.coderGtm.yantra.SYSTEM_PKG_SETTINGS
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherWorkspaceTest {

    @Test
    fun testHomeLayoutToWorkspaceRoundTrip() {
        val originalLayout = HomeLayout(
            pages = listOf(
                HomePage(
                    index = 0,
                    items = listOf(
                        HomeItem(SYSTEM_PKG_CLI, 0, 0),
                        HomeItem("com.android.chrome", 1, 0)
                    )
                ),
                HomePage(
                    index = 1,
                    items = listOf(
                        HomeItem("com.android.settings", 0, 1)
                    )
                )
            ),
            dockPackages = listOf(SYSTEM_PKG_CLI, SYSTEM_PKG_SETTINGS, "com.android.dialer")
        )

        val workspace = originalLayout.toLauncherWorkspace()
        assertEquals(2, workspace.pages.size)
        assertEquals(2, workspace.pages[0].items.size)
        assertEquals(true, workspace.pages[0].items[0] is LauncherItem.SystemAction)
        assertEquals(true, workspace.pages[0].items[1] is LauncherItem.App)

        val roundTripLayout = workspace.toHomeLayout()
        assertEquals(originalLayout, roundTripLayout)
    }
}

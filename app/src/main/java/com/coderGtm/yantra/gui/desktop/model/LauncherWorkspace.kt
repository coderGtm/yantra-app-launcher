package com.coderGtm.yantra.gui.desktop.model

import com.coderGtm.yantra.SYSTEM_PKG_CLI
import com.coderGtm.yantra.SYSTEM_PKG_SETTINGS

sealed interface LauncherItem {
    val column: Int
    val row: Int

    data class App(
        val packageName: String,
        override val column: Int,
        override val row: Int
    ) : LauncherItem

    data class SystemAction(
        val actionId: String,
        override val column: Int,
        override val row: Int
    ) : LauncherItem

    // Future extension variants (Folders, Widgets, etc.)
}

data class LauncherPage(
    val index: Int,
    val items: List<LauncherItem> = emptyList()
)

data class LauncherContainer(
    val items: List<LauncherItem> = emptyList()
)

data class LauncherWorkspace(
    val pages: List<LauncherPage> = listOf(LauncherPage(0)),
    val dockPackages: List<String> = emptyList()
)

fun HomeItem.toLauncherItem(): LauncherItem {
    return if (packageName == SYSTEM_PKG_CLI || packageName == SYSTEM_PKG_SETTINGS) {
        LauncherItem.SystemAction(actionId = packageName, column = column, row = row)
    } else {
        LauncherItem.App(packageName = packageName, column = column, row = row)
    }
}

fun LauncherItem.toHomeItem(): HomeItem {
    return when (this) {
        is LauncherItem.App -> HomeItem(packageName = packageName, column = column, row = row)
        is LauncherItem.SystemAction -> HomeItem(packageName = actionId, column = column, row = row)
    }
}

fun HomePage.toLauncherPage(): LauncherPage {
    return LauncherPage(
        index = index,
        items = items.map { it.toLauncherItem() }
    )
}

fun LauncherPage.toHomePage(): HomePage {
    return HomePage(
        index = index,
        items = items.map { it.toHomeItem() }
    )
}

fun HomeLayout.toLauncherWorkspace(): LauncherWorkspace {
    return LauncherWorkspace(
        pages = pages.map { it.toLauncherPage() },
        dockPackages = dockPackages
    )
}

fun LauncherWorkspace.toHomeLayout(): HomeLayout {
    return HomeLayout(
        pages = pages.map { it.toHomePage() },
        dockPackages = dockPackages
    )
}

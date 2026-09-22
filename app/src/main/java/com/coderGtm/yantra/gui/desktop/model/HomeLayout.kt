package com.coderGtm.yantra.gui.desktop.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeItem(
    val packageName: String,
    val column: Int,
    val row: Int
)

@Serializable
data class HomePage(
    val index: Int,
    val items: List<HomeItem> = emptyList()
)

@Serializable
data class HomeLayout(
    val pages: List<HomePage> = listOf(HomePage(0)),
    val dockPackages: List<String> = emptyList()
)

package com.coderGtm.yantra.gui.desktop.model

data class DesktopLayout(

    val columns: Int = 4,

    val rows: Int = 6,

    val items: MutableList<DesktopItem> = mutableListOf()

)
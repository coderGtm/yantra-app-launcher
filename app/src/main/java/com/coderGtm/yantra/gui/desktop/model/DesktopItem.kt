package com.coderGtm.yantra.gui.desktop.model

import android.graphics.drawable.Drawable

data class DesktopItem(

    val id: String,

    val label: String,

    val packageName: String?,

    val icon: Drawable?,

    val type: DesktopItemType,

    val position: GridPosition,

    val anchor: Anchor = Anchor.TOP_LEFT,

    val width: Int = 1,

    val height: Int = 1

)
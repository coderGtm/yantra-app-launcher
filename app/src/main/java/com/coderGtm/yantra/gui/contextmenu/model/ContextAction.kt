package com.coderGtm.yantra.gui.contextmenu.model

data class ContextAction(

    val id: String,

    val title: String,

    val iconRes: Int,

    val enabled: Boolean = true

)
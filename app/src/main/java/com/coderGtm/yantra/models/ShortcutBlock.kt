package com.coderGtm.yantra.models

import android.os.UserHandle

data class ShortcutBlock(
    val label:String,
    val packageName:String,
    val id: String,
    val user: UserHandle
)

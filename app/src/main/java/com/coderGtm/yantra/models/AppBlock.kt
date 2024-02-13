package com.coderGtm.yantra.models

import android.os.UserHandle

data class AppBlock (
    val appName:String,
    val packageName:String,
    val user: UserHandle,
    val category: Int = -1
)

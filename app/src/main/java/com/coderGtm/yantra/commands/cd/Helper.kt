package com.coderGtm.yantra.commands.cd

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import com.coderGtm.yantra.terminal.Terminal
import java.io.File

fun getPathIfExists(path: String): String? {
    val file = File(Environment.getExternalStorageDirectory().absolutePath + path)
    return if (file.exists()) {
        file.path.toString().substring(Environment.getExternalStorageDirectory().absolutePath.length)
    } else {
        null
    }
}

fun list(path: String, command: Command) {
    val files = File(Environment.getExternalStorageDirectory().absolutePath + path).listFiles()
    if (files == null) {
        command.output("Empty", command.terminal.theme.resultTextColor)
        return
    }

    for (file in files) {
        println(file.name)
    }
}

fun checkPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true
    }
}
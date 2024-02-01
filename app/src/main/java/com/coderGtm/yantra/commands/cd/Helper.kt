package com.coderGtm.yantra.commands.cd

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

fun getPathIfExists(path: String): String? {
    val file = File(Environment.getExternalStorageDirectory().absolutePath + path)
    return if (file.isDirectory) {
        file.path.toString().substring(Environment.getExternalStorageDirectory().absolutePath.length)
    } else {
        null
    }
}

fun checkPermission(command: Command): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(command.terminal.activity,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}
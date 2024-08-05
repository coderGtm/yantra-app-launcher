package com.coderGtm.yantra.commands.open

import android.content.Intent

fun isExists(path: String): Boolean {
    // no check as of now
    return true
}

fun openFile(pathToFile: String, command: Command) {
    val intent = Intent()
    intent.setClassName(
        "com.anready.croissant",
        "com.anready.croissant.providers.OpenFile"
    )
    intent.putExtra("path", pathToFile) // pathToFile = "/DCIM/Camera/img.jpg"
    command.terminal.activity.startActivity(intent)
}
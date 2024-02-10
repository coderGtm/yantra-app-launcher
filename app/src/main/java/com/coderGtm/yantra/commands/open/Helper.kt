package com.coderGtm.yantra.commands.open

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.coderGtm.yantra.BuildConfig
import java.io.File

fun isExists(path: String): Boolean {
    val file = File(path)
    return file.isFile
}

fun openFiles(file: File, command: Command) {
    val fileUri = Uri.fromFile(file)

    val contentResolver = command.terminal.activity.contentResolver
    val mimeType = contentResolver.getType(fileUri)

    val uri = FileProvider.getUriForFile(
        command.terminal.activity,
        "${BuildConfig.APPLICATION_ID}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    command.terminal.activity.startActivity(intent)
}
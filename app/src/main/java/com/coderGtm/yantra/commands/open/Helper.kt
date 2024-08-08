package com.coderGtm.yantra.commands.open

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import org.json.JSONArray
import org.json.JSONException

fun isPathExist(command: Command, path: String): Boolean {
    val contentResolver: ContentResolver = command.terminal.activity.contentResolver
    val uri = Uri.parse("content://com.anready.croissant.files")
        .buildUpon()
        .appendQueryParameter("path", path)
        .appendQueryParameter("command", "pathExist")
        .build()

    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val dataIndex = cursor.getColumnIndex("response")
            if (dataIndex == -1) {
                command.output("Data not found", command.terminal.theme.errorTextColor)
                return false
            }

            val jsonArray = JSONArray(cursor.getString(dataIndex))
            if (error(jsonArray)) {
                command.output(jsonArray.getJSONObject(0).getString("error").toString(), command.terminal.theme.errorTextColor)
                return false
            }

            val fileInfo = jsonArray.getJSONObject(0)
            return fileInfo.getBoolean("result")
        } else {
            command.output("Error while getting data!", command.terminal.theme.errorTextColor)
        }
    } catch (e: Exception) {
        command.output("Error while getting data!\n" + e.message, command.terminal.theme.errorTextColor)
    } finally {
        cursor?.close()
    }
    return false
}

private fun error(jsonArray: JSONArray): Boolean { //Method of getting error
    try {
        val error = jsonArray.getJSONObject(0)
        error.getString("error")
        return true
    } catch (e: JSONException) {
        return false
    }
}

fun openFile(pathToFile: String, command: Command) {
    val intent = Intent()
    intent.setClassName(
        "com.anready.croissant",
        "com.anready.croissant.providers.OpenFile"
    )
    intent.putExtra("path", pathToFile)
    command.terminal.activity.startActivity(intent)
}
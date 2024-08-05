package com.coderGtm.yantra.commands.cd

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import org.json.JSONArray
import org.json.JSONException


fun getPathIfExists(activity: Activity, path: String): String? {
    val contentResolver: ContentResolver = activity.contentResolver
    val uri = Uri.parse("content://com.anready.croissant.files")
        .buildUpon()
        .appendQueryParameter("path", path) // Providing path
        .appendQueryParameter("command", "list") // Set command to list
        .build()

    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val dataIndex = cursor.getColumnIndex("response")
            if (dataIndex == -1) {
                println("Data column not found")
                return null
            }

            val jsonArray = JSONArray(cursor.getString(dataIndex))
            if (error(jsonArray)) { //Checking response on error
                println("Error: " + jsonArray.getJSONObject(0).getString("error"))
                return null
            }

            for (i in 0 until jsonArray.length()) {
                val fileInfo = jsonArray.getJSONObject(i)
                if (fileInfo.getString("name") == path && fileInfo.getBoolean("type")) {
                    return path
                }
            }

            return null
        } else {
            println("Error while getting data!")
        }
    } catch (e: Exception) {
        println("Error while getting data!\n" + e.message)
    } finally {
        cursor?.close()
    }

    return null
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
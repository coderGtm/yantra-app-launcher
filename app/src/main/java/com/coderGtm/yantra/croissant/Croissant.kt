package com.coderGtm.yantra.croissant

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.commands.open.Command
import com.coderGtm.yantra.models.DirectoryContents
import com.coderGtm.yantra.terminal.Terminal
import org.json.JSONArray
import org.json.JSONException

class Croissant {
    private fun main(activity: Activity, command: String, path: String = "/"): JSONArray {
        val contentResolver: ContentResolver = activity.contentResolver
        val uri = Uri.parse("content://com.anready.croissant.files")
            .buildUpon()
            .appendQueryParameter("command", command) // Adding parameter command
            .appendQueryParameter("path", path)
            .build()

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val dataIndex = cursor.getColumnIndex("response")
                if (dataIndex == -1) {
                    return JSONArray(arrayOf("Error while getting data!"))
                }

                return JSONArray(cursor.getString(dataIndex))
            } else {
                return JSONArray(arrayOf("Error while getting data!"))
            }
        } catch (e: Exception) {
            return JSONArray(arrayOf("Error while getting data!" + e.message))
        } finally {
            cursor?.close()
        }
    }

    fun checkCroissantPermission(activity: Activity):Boolean {
        val fileInfo = main(activity, "isPermissionsGranted").getJSONObject(0)
        return fileInfo.getBoolean("result")
    }

    fun getListOfObjects(terminal: Terminal, path: String): MutableList<DirectoryContents> {
        val jsonArray = main(terminal.activity, "list", path)

        if (jsonArray.length() > 0 && jsonArray.getJSONObject(0).equals("Error while getting data!")) {
            terminal.output("Data not found", terminal.theme.errorTextColor, null, false)
            return mutableListOf()
        }

        if (error(jsonArray)) { //Checking response on error
            terminal.output(jsonArray.getJSONObject(0).getString("error").toString(), terminal.theme.errorTextColor, null, false)
            return mutableListOf()
        }

        val fullList = mutableListOf<DirectoryContents>()

        for (i in 0 until jsonArray.length()) {
            val fileInfo = jsonArray.getJSONObject(i)
            fullList.add(
                DirectoryContents(
                    name = fileInfo.getString("name"),
                    isDirectory = fileInfo.getBoolean("type"),
                    isHidden = fileInfo.getBoolean("visibility")
                )
            )
        }

        return fullList
    }

    fun isPathExist(terminal: Terminal, path: String): Boolean {
        val jsonArray = main(terminal.activity, "pathExist", path)
        if (jsonArray.length() > 0 && jsonArray.getJSONObject(0).equals("Error while getting data!")) {
            terminal.output("Data not found", terminal.theme.errorTextColor, null)
            return false
        }

        if (error(jsonArray)) {
            terminal.output(jsonArray.getJSONObject(0).getString("error").toString(), terminal.theme.errorTextColor, null)
            return false
        }

        val fileInfo = jsonArray.getJSONObject(0)
        return fileInfo.getBoolean("result")
    }

    fun openFile(pathToFile: String, terminal: Terminal) {
        val intent = Intent()
        intent.setClassName(
            "com.anready.croissant",
            "com.anready.croissant.providers.OpenFile"
        )
        intent.putExtra("path", pathToFile)
        val mainAct = terminal.activity as MainActivity
        mainAct.openResultLauncher.launch(intent)
    }

    fun error(jsonArray: JSONArray): Boolean { //Method of getting error
        try {
            val error = jsonArray.getJSONObject(0)
            error.getString("error")
            return true
        } catch (e: JSONException) {
            return false
        }
    }
}
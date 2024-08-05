package com.coderGtm.yantra.commands.ls

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.checkCroissantPermission
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.models.DirectoryContents
import com.coderGtm.yantra.terminal.Terminal
import org.json.JSONArray
import org.json.JSONException

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "ls",
        helpTitle = "ls [-a]",
        description = terminal.activity.getString(R.string.cmd_ls_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)
        var showHidden = false

        if (args.size == 1) {
            if (args.first().trim() == "-a") {
                showHidden = true
            }
            else {
                output(terminal.activity.getString(R.string.ls_invalid_arg), terminal.theme.errorTextColor)
                return
            }
        }
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.ls_many_args), terminal.theme.errorTextColor)
            return
        }

        if (!checkCroissantPermission(terminal.activity)) {
            output("Croissant app does not seem to have the required permissions.", terminal.theme.warningTextColor)
            return
        }

        val files = getListOfObjects(terminal.activity, terminal.workingDir)

        if (files.isEmpty()) {
            return
        }

        for (obj in files) {
            if (obj.isHidden && !showHidden) {
                continue
            }
            if (obj.isDirectory) {
                output(obj.name, terminal.theme.warningTextColor, Typeface.BOLD)
            }
            else {
                output(obj.name, terminal.theme.resultTextColor)
            }
        }
    }


    private fun getListOfObjects(ac: Activity, path: String): MutableList<DirectoryContents> {
        val contentResolver: ContentResolver = ac.contentResolver
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
                    //println("Data column not found")
                    return mutableListOf()
                }

                val jsonArray = JSONArray(cursor.getString(dataIndex))
                if (error(jsonArray)) { //Checking response on error
                    //println("Error: " + jsonArray.getJSONObject(0).getString("error"))
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
            } else {
                //println("Error while getting data!")
            }
        } catch (e: Exception) {
            //println("Error while getting data!\n" + e.message)
        } finally {
            cursor?.close()
        }

        return mutableListOf()
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
}
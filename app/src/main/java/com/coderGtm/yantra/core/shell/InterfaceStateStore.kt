package com.coderGtm.yantra.core.shell

import android.content.Context
import org.json.JSONObject
import java.io.File

class InterfaceStateStore(context: Context) {
    private val stateFile = File(context.filesDir, "yantra/interface_state.json")

    fun save(shellId: String) {
        try {
            stateFile.parentFile?.mkdirs()
            val json = JSONObject().apply {
                put("interface", shellId)
                put("timestamp", System.currentTimeMillis())
            }
            stateFile.writeText(json.toString(), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load(): String? {
        if (!stateFile.exists()) return null
        return try {
            val json = JSONObject(stateFile.readText(Charsets.UTF_8))
            if (json.has("interface")) json.getString("interface") else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

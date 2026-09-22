package com.coderGtm.yantra.gui.package_manager

import org.json.JSONObject

object GuiManifestParser {
    fun parse(jsonText: String): GuiPackageManifest {
        val json = JSONObject(jsonText)

        val schema = json.optInt("yantra_gui", -1)
        require(schema == 1) { "Unsupported yantra_gui version: $schema" }

        return GuiPackageManifest(
            id = json.getString("id").trim(),
            name = json.getString("name").trim(),
            version = json.getString("version").trim(),
            author = json.optString("author", "Unknown").trim().ifEmpty { "Unknown" },
            api = json.getString("api").trim(),
            entry = json.getString("entry").trim(),
            stylesheet = json.getString("stylesheet").trim(),
            script = json.getString("script").trim(),
            wallpaper = json.optString("wallpaper", "system").trim().ifEmpty { "system" }
        )
    }
}

data class GuiPackageManifest(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val api: String,
    val entry: String,
    val stylesheet: String,
    val script: String,
    val wallpaper: String
)

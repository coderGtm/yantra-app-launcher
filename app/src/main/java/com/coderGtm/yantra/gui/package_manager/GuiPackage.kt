package com.coderGtm.yantra.gui.package_manager

import java.io.File

/** Metadata and local storage location for an imported Yantra GUI package. */
data class GuiPackage(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val api: String,
    val entry: String,
    val stylesheet: String,
    val script: String,
    val wallpaper: String,
    val rootDir: File,
    val builtIn: Boolean = false
)

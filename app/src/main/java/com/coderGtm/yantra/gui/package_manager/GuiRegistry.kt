package com.coderGtm.yantra.gui.package_manager

import android.content.Context
import org.json.JSONObject
import java.io.File

class GuiRegistry(context: Context) {
    private val root = File(context.filesDir, "yantra/gui_packages")
    private val packages = linkedMapOf<String, GuiPackage>()

    init {
        root.mkdirs()
        reload()
    }

    @Synchronized
    fun reload() {
        packages.clear()
        root.listFiles()
            ?.filter { it.isDirectory }
            ?.forEach { dir ->
                runCatching { loadPackage(dir) }
            }
    }

    @Synchronized
    fun all(): List<GuiPackage> = packages.values.sortedBy { it.id.lowercase() }

    @Synchronized
    fun get(id: String): GuiPackage? = packages[id.lowercase()]

    @Synchronized
    fun contains(id: String): Boolean = packages.containsKey(id.lowercase())

    @Synchronized
    fun remove(id: String): Boolean {
        val pkg = packages.remove(id.lowercase()) ?: return false
        return pkg.rootDir.deleteRecursively()
    }

    @Synchronized
    fun upsert(rootDir: File, manifest: GuiPackageManifest): GuiPackage {
        val packageInfo = GuiPackage(
            id = manifest.id,
            name = manifest.name,
            version = manifest.version,
            author = manifest.author,
            api = manifest.api,
            entry = manifest.entry,
            stylesheet = manifest.stylesheet,
            script = manifest.script,
            wallpaper = manifest.wallpaper,
            rootDir = rootDir
        )
        packages[manifest.id.lowercase()] = packageInfo
        return packageInfo
    }

    fun packageRoot(): File = root

    private fun loadPackage(dir: File) {
        val manifestFile = File(dir, "manifest.json")
        if (!manifestFile.isFile) return
        val manifest = GuiManifestParser.parse(manifestFile.readText(Charsets.UTF_8))
        val errors = GuiValidator.validate(dir, manifest)
        if (errors.isEmpty()) upsert(dir, manifest)
    }
}

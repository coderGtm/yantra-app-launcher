package com.coderGtm.yantra.gui.package_manager

import java.io.File

object GuiValidator {
    private val safeId = Regex("^[A-Za-z0-9._-]+$")

    fun validate(root: File, manifest: GuiPackageManifest): List<String> {
        val errors = mutableListOf<String>()

        if (!safeId.matches(manifest.id)) {
            errors += "Invalid GUI id: ${manifest.id}"
        }
        if (manifest.name.isBlank()) errors += "Manifest field 'name' is empty."
        if (manifest.version.isBlank()) errors += "Manifest field 'version' is empty."
        if (manifest.api.isBlank()) errors += "Manifest field 'api' is empty."

        validateRelativePath("entry", manifest.entry, errors)
        validateRelativePath("stylesheet", manifest.stylesheet, errors)
        validateRelativePath("script", manifest.script, errors)

        if (manifest.wallpaper != "system") {
            validateRelativePath("wallpaper", manifest.wallpaper, errors)
        }

        val entry = safeResolve(root, manifest.entry)
        val stylesheet = safeResolve(root, manifest.stylesheet)
        val script = safeResolve(root, manifest.script)

        if (entry == null || !entry.isFile) errors += "Entry HTML not found: ${manifest.entry}"
        if (stylesheet == null || !stylesheet.isFile) errors += "Stylesheet not found: ${manifest.stylesheet}"
        if (script == null || !script.isFile) errors += "Script not found: ${manifest.script}"

        if (errors.isNotEmpty()) return errors

        val html = entry!!.readText(Charsets.UTF_8)
        val css = stylesheet!!.readText(Charsets.UTF_8)
        val js = script!!.readText(Charsets.UTF_8)

        if (html.isBlank()) errors += "Entry HTML is empty."
        if (css.isBlank()) errors += "Stylesheet is empty."
        if (js.isBlank()) errors += "Script is empty."

        // Required bridge route from the GUI back to the Yantra CLI.
        val hasCliAction = Regex(
            "<yantra-action\\b[^>]*\\baction\\s*=\\s*[\\\"']cli[\\\"'][^>]*>",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(html)
        if (!hasCliAction) {
            errors += "Required <yantra-action action=\"cli\"> element is missing."
        }

        // Cheap structural HTML sanity checks. Full HTML/JS parsing belongs to the WebView runtime.
        if (!html.contains("<html", ignoreCase = true) || !html.contains("</html>", ignoreCase = true)) {
            errors += "HTML document is missing <html> structure."
        }
        if (!html.contains("<body", ignoreCase = true) || !html.contains("</body>", ignoreCase = true)) {
            errors += "HTML document is missing <body> structure."
        }

        return errors
    }

    private fun validateRelativePath(field: String, path: String, errors: MutableList<String>) {
        val normalized = path.replace('\\', '/')
        if (normalized.isBlank() || normalized.startsWith("/") || normalized.contains("../") || normalized == "..") {
            errors += "Invalid $field path: $path"
        }
    }

    private fun safeResolve(root: File, relativePath: String): File? {
        val candidate = File(root, relativePath.replace('\\', '/'))
        val rootCanonical = root.canonicalFile
        val candidateCanonical = candidate.canonicalFile
        return if (candidateCanonical.path == rootCanonical.path ||
            candidateCanonical.path.startsWith(rootCanonical.path + File.separator)
        ) candidateCanonical else null
    }
}

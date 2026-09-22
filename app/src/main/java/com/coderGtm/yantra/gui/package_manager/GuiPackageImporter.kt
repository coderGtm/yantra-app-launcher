package com.coderGtm.yantra.gui.package_manager

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.File
import java.io.IOException

class GuiPackageImporter(private val context: Context, private val registry: GuiRegistry) {

    data class Result(
        val success: Boolean,
        val packageInfo: GuiPackage? = null,
        val errors: List<String> = emptyList()
    )

    fun import(uri: Uri): Result {
        val stagingRoot = File(context.cacheDir, "yantra_gui_import_${System.currentTimeMillis()}")
        val packageRoot = registry.packageRoot()

        return try {
            stagingRoot.deleteRecursively()
            stagingRoot.mkdirs()
            copyTree(uri, stagingRoot)

            val manifestFile = File(stagingRoot, "manifest.json")
            if (!manifestFile.isFile) {
                return Result(false, errors = listOf("manifest.json was not found in the selected folder."))
            }

            val manifest = try {
                GuiManifestParser.parse(manifestFile.readText(Charsets.UTF_8))
            } catch (e: Exception) {
                return Result(false, errors = listOf("Invalid manifest.json: ${e.message ?: "parse error"}"))
            }

            val errors = GuiValidator.validate(stagingRoot, manifest)
            if (errors.isNotEmpty()) return Result(false, errors = errors)

            val target = File(packageRoot, manifest.id)
            val oldTarget = File(packageRoot, "${manifest.id}.old_${System.currentTimeMillis()}")

            if (target.exists()) {
                if (!target.renameTo(oldTarget)) {
                    return Result(false, errors = listOf("Could not replace the existing GUI '${manifest.id}'."))
                }
            }

            if (!stagingRoot.renameTo(target)) {
                if (oldTarget.exists()) oldTarget.renameTo(target)
                return Result(false, errors = listOf("Could not install GUI '${manifest.id}'."))
            }

            oldTarget.deleteRecursively()
            val pkg = registry.upsert(target, manifest)
            Result(true, pkg)
        } catch (e: Exception) {
            Result(false, errors = listOf("Import failed: ${e.message ?: e.javaClass.simpleName}"))
        } finally {
            if (stagingRoot.exists()) stagingRoot.deleteRecursively()
        }
    }

    private fun copyTree(treeUri: Uri, destination: File) {
        val resolver = context.contentResolver
        val documentId = DocumentsContract.getTreeDocumentId(treeUri)
        copyDocument(Uri.parse("${DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)}"), destination)
    }

    private fun copyDocument(documentUri: Uri, destination: File) {
        val resolver = context.contentResolver
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID
        )
        val cursor = resolver.query(documentUri, projection, null, null, null)
            ?: throw IOException("Unable to read selected folder.")

        cursor.use {
            if (!it.moveToFirst()) throw IOException("Selected folder is empty or inaccessible.")
            val mime = it.getString(1)
            if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                destination.mkdirs()
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    documentUri,
                    DocumentsContract.getDocumentId(documentUri)
                )
                val childCursor = resolver.query(childrenUri, projection, null, null, null)
                    ?: throw IOException("Unable to list GUI files.")
                childCursor.use { children ->
                    while (children.moveToNext()) {
                        val name = children.getString(0) ?: continue
                        val childMime = children.getString(1) ?: ""
                        if (name == "." || name == ".." || name.contains('/') || name.contains('\\')) continue
                        val childDocumentId = children.getString(
                            children.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                        )
                        val childUri = DocumentsContract.buildDocumentUriUsingTree(documentUri, childDocumentId)
                        val childTarget = File(destination, name)
                        if (childMime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            copyDocument(childUri, childTarget)
                        } else {
                            copyFile(childUri, childTarget)
                        }
                    }
                }
            } else {
                copyFile(documentUri, destination)
            }
        }
    }

    private fun copyFile(uri: Uri, destination: File) {
        destination.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Unable to read ${destination.name}.")
    }
}

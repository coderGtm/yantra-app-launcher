package com.coderGtm.yantra.activities.helpers

import android.content.Intent
import android.graphics.Typeface
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.core.widget.addTextChangedListener
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.DEFAULT_TERMINAL_FONT_NAME
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.SettingsActivity
import com.coderGtm.yantra.misc.changedSettingsCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

internal fun SettingsActivity.openFontSelector() {
    Toast.makeText(this, getString(R.string.loading_fonts), Toast.LENGTH_SHORT).show()
    val queue = Volley.newRequestQueue(this)
    val stringRequest = StringRequest(
        Request.Method.GET,
        "https://www.googleapis.com/webfonts/v1/webfonts?key=AIzaSyBFFPy6DsYRRQVlADHdCgKk5qd62CJxjqo",
        { response ->
            val jsonArray = JSONObject(response).getJSONArray("items")
            val names = ArrayList<String>()
            for (i in 0 until jsonArray.length()) names.add(jsonArray.getJSONObject(i).getString("family"))
            for (name in getAllFonts()) names.add(name)
            names.sort()

            val adapter    = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
            val dialogView = layoutInflater.inflate(R.layout.dialog_font_list, null) as View
            val fontSelector = MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.select_a_font))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.import_local_font)) { d, _ -> importFontFromFile(); d.dismiss() }
                .setNegativeButton(getString(R.string.close)) { d, _ -> d.cancel() }
                .create()

            if (!isFinishing) {
                val listView  = dialogView.findViewById<ListView>(R.id.fontList)
                val searchBar = dialogView.findViewById<EditText>(R.id.searchBar)
                listView.adapter = adapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    val selected = adapter.getItem(position) ?: DEFAULT_TERMINAL_FONT_NAME
                    if (selected.endsWith(".ttf")) {
                        preferenceEditObject.putString("font", selected).apply()
                        fontName = selected.replace(".ttf", "")
                        Toast.makeText(this, getString(R.string.terminal_font_updated_to, selected), Toast.LENGTH_SHORT).show()
                        changedSettingsCallback(this)
                        fontSelector.cancel()
                        return@setOnItemClickListener
                    }
                    downloadFont(selected)
                    fontSelector.cancel()
                }
                searchBar.addTextChangedListener { text -> adapter.filter.filter(text) }
                fontSelector.show()
            }
        },
        { error ->
            val msg = if (error is NoConnectionError) getString(R.string.no_internet_connection)
                      else getString(R.string.an_error_occurred_please_try_again)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    )
    Thread { queue.add(stringRequest) }.start()
}

internal fun SettingsActivity.importFontFromFile() {
    selectFontLauncher.launch(
        Intent.createChooser(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "font/ttf"
            },
            getString(R.string.select_a_font_file)
        )
    )
}

internal fun SettingsActivity.getAllFonts(): List<String> =
    filesDir.listFiles()?.filter { !it.isDirectory && it.name.endsWith(".ttf") }?.map { it.name } ?: emptyList()

internal fun SettingsActivity.downloadFont(name: String) {
    val request = FontRequest(
        "com.google.android.gms.fonts",
        "com.google.android.gms",
        name,
        R.array.com_google_android_gms_fonts_certs
    )
    val callback = object : FontsContractCompat.FontRequestCallback() {
        override fun onTypefaceRetrieved(typeface: Typeface) {
            preferenceEditObject.putString("font", name).apply()
            fontName = name
            Toast.makeText(this@downloadFont, getString(R.string.terminal_font_updated_to, name), Toast.LENGTH_SHORT).show()
            changedSettingsCallback(this@downloadFont)
        }
        override fun onTypefaceRequestFailed(reason: Int) {
            Toast.makeText(this@downloadFont, getString(R.string.error_downloading_font), Toast.LENGTH_LONG).show()
        }
    }
    @Suppress("DEPRECATION")
    FontsContractCompat.requestFont(this, request, callback, Handler())
}


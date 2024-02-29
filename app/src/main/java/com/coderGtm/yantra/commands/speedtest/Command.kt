package com.coderGtm.yantra.commands.speedtest

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "speedtest",
        helpTitle = "speedtest",
        description = terminal.activity.getString(R.string.cmd_speedtest_help)
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmd_takes_no_params, metadata.name), terminal.theme.errorTextColor)
            return
        }
        output(terminal.activity.getString(R.string.loading_speedtest_utility), terminal.theme.successTextColor)
        val view = LayoutInflater.from(terminal.activity).inflate(R.layout.dialog_speedtest , null)
        val dialog = MaterialAlertDialogBuilder(terminal.activity)
            .setView(view)
            .setCancelable(false)
            .show()
        val webView = dialog.findViewById<WebView>(R.id.webView)
        webView?.settings?.javaScriptEnabled = true
        webView?.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                dialog.findViewById<TextView>(R.id.loadingText)?.visibility = android.view.View.GONE
            }
        }
        dialog.findViewById<Button>(R.id.closeBtn)?.setOnClickListener {
            dialog.dismiss()
        }
        webView?.loadUrl("file:///android_asset/speedtest.html")
    }
}
package com.coderGtm.yantra.commands.search

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal


fun isSearchEngineAppInstalled(engine: String, appList: List<AppBlock>): Boolean {
    val packageName = getPackageName(engine)
    return isPackageInstalled(packageName, appList)
}

fun isPackageInstalled(packageName: String, appList: List<AppBlock>): Boolean {
    return appList.any { it.packageName == packageName }
}

fun openUrlInApp(url: String, query: String, packageName: String, terminal: Terminal) {
    val intent = Intent(Intent.ACTION_WEB_SEARCH)
    intent.setPackage(packageName)
    intent.putExtra(SearchManager.QUERY, query)
    if (intent.resolveActivity(terminal.activity.packageManager) != null) {
        terminal.activity.startActivity(intent)
    }
    //else search in browser
    else {
        openURL(url, terminal.activity)
    }
}

fun getPackageName(engine: String): String {
    return when (engine) {
        "google" -> "com.google.android.googlequicksearchbox"
        "duckduckgo" -> "com.duckduckgo.mobile.android"
        "bing" -> "com.microsoft.bing"
        "yahoo" -> "com.yahoo.mobile.client.android.search"
        "ecosia" -> "com.ecosia.android"
        "startpage" -> "com.startpage.search"
        "qwant" -> "com.qwant.liberty"
        "you" -> "com.you.app"
        else -> ""
    }
}
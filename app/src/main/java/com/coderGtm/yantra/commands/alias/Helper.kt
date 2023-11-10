package com.coderGtm.yantra.commands.alias

import android.content.SharedPreferences
import com.coderGtm.yantra.models.Alias

fun updateAliasList(aliasList: MutableList<Alias>, preferenceEditObject: SharedPreferences.Editor) {
    val aliasList2 = mutableListOf<String>()
    for (i in aliasList.indices) {
        aliasList2.add(aliasList[i].key + "=" + aliasList[i].value)
    }
    preferenceEditObject.putStringSet("aliasList", aliasList2.toSet()).apply()
}
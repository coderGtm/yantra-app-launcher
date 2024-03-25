package com.coderGtm.yantra.commands.todo

import android.content.SharedPreferences

fun getToDo(preferenceObject: SharedPreferences): MutableSet<String> {
    return preferenceObject.getStringSet("todoList", setOf())!!
        .map { it.split(":", limit = 2).let { (index, value) -> index.toInt() to value } }
        .sortedBy { it.first }
        .mapTo(mutableSetOf()) { it.second }
}

fun getToDoProgressList(sizeOfTodo: Int, preferenceObject: SharedPreferences): java.util.ArrayList<Int> {
    if (sizeOfTodo == 0) return arrayListOf(0)
    var progress = arrayListOf<Int>()
    val ps = preferenceObject.getString("todoProgressList", "") ?: ""
    if (ps.trim() == "") {
        for (i in 0 until sizeOfTodo) {
            progress.add(0)
        }
        return progress
    }
    else {
        progress = ps.split(";").map {
            it.toInt()
        } as java.util.ArrayList<Int>
        if (progress.size == sizeOfTodo) {
            return progress
        }
        else {
            progress = arrayListOf()
            for (i in 0 until sizeOfTodo) {
                progress.add(0)
            }
            return progress
        }
    }
}

fun setToDoProgress(todoProgressList: java.util.ArrayList<Int>, preferenceEditObject: SharedPreferences.Editor) {
    val ps = todoProgressList.joinToString(";")
    preferenceEditObject.putString("todoProgressList", ps).apply()
}

fun setToDo(list: MutableSet<String>?, preferenceEditObject: SharedPreferences.Editor) {
    preferenceEditObject.putStringSet("todoList", list?.mapIndexedTo(mutableSetOf()) { index, s -> "$index:$s" }).apply()
}
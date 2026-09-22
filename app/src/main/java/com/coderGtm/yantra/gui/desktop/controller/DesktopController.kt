package com.coderGtm.yantra.gui.desktop.controller

import android.content.Context
import com.coderGtm.yantra.gui.desktop.model.DesktopLayout
import com.coderGtm.yantra.gui.desktop.repository.DesktopRepository
import com.coderGtm.yantra.gui.drawer.repository.AppRepository

class DesktopController(
    context: Context
) {

    private val repository = DesktopRepository(
        AppRepository(context)
    )

    private var layout: DesktopLayout =
        repository.loadLayout()

    fun getLayout(): DesktopLayout {

        return repository.loadLayout()

    }

    fun reload() {
        layout = repository.reloadLayout()
    }

    fun reset() {
        layout = repository.resetLayout()
    }

    fun save() {
        repository.saveLayout(layout)
    }
}
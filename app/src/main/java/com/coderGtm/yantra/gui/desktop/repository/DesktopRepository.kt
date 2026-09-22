package com.coderGtm.yantra.gui.desktop.repository

import com.coderGtm.yantra.gui.desktop.model.Anchor
import com.coderGtm.yantra.gui.desktop.model.DesktopItem
import com.coderGtm.yantra.gui.desktop.model.DesktopItemType
import com.coderGtm.yantra.gui.desktop.model.DesktopLayout
import com.coderGtm.yantra.gui.desktop.model.GridPosition
import com.coderGtm.yantra.gui.drawer.repository.AppRepository

class DesktopRepository(
    private val appRepository: AppRepository
) {

    /**
     * Returns the launcher's default desktop layout.
     * Later this will be loaded from XML / JSON / Database.
     */
    fun loadLayout(): DesktopLayout {

        val layout = DesktopLayout()

        return layout
    }

    /**
     * Placeholder.
     * Will later save the user's desktop configuration.
     */
    fun saveLayout(layout: DesktopLayout) {

        // TODO:
        // XML
        // JSON
        // Room
        // SQLite

    }

    /**
     * Placeholder.
     * Will later restore the last saved layout.
     */
    fun reloadLayout(): DesktopLayout {

        return loadLayout()

    }

    /**
     * Reset desktop to factory layout.
     */
    fun resetLayout(): DesktopLayout {

        return loadLayout()

    }

}
package com.coderGtm.yantra.gui.desktop.controller

import com.coderGtm.yantra.gui.desktop.model.HomeItem
import com.coderGtm.yantra.gui.desktop.model.HomePage
import com.coderGtm.yantra.gui.desktop.repository.HomeRepository
import com.coderGtm.yantra.ui.screens.gui.NativeGuiState

class HomeLayoutController(
    private val repository: HomeRepository,
    private val state: NativeGuiState
) {
    fun addOrMoveToHome(packageName: String, pageIndex: Int = 0, column: Int, row: Int) {
        val currentLayout = state.layout
        val pages = currentLayout.pages.toMutableList()
        
        // Ensure the page exists
        while (pages.size <= pageIndex) {
            pages.add(HomePage(pages.size))
        }
        
        // Remove packageName from anywhere in home layout to prevent duplicates
        val cleanedPages = pages.map { page ->
            page.copy(items = page.items.filter { it.packageName != packageName })
        }.toMutableList()
        
        val targetPage = cleanedPages[pageIndex]
        val items = targetPage.items.toMutableList()
        
        // Handle occupied target cell (relocate occupant to first empty spot)
        val occupyingIndex = items.indexOfFirst { it.column == column && it.row == row }
        if (occupyingIndex != -1) {
            val occupant = items[occupyingIndex]
            items.removeAt(occupyingIndex)
            val emptySpot = findFirstEmptySpot(items)
            if (emptySpot != null) {
                items.add(HomeItem(occupant.packageName, emptySpot.first, emptySpot.second))
            } else {
                items.add(occupant)
            }
        }
        
        items.add(HomeItem(packageName, column, row))
        cleanedPages[pageIndex] = targetPage.copy(items = items)
        
        val newLayout = currentLayout.copy(pages = cleanedPages)
        state.layout = newLayout
        repository.save(newLayout)
    }

    fun addToHome(packageName: String, pageIndex: Int = 0, column: Int? = null, row: Int? = null) {
        val currentLayout = state.layout
        val pages = currentLayout.pages.toMutableList()
        
        // Ensure the page exists
        while (pages.size <= pageIndex) {
            pages.add(HomePage(pages.size))
        }
        
        val currentPage = pages[pageIndex]
        val items = currentPage.items.toMutableList()
        
        if (items.any { it.packageName == packageName }) return
        
        val (finalCol, finalRow) = if (column != null && row != null) {
            if (items.any { it.column == column && it.row == row }) {
                findFirstEmptySpot(items) ?: return // No room
            } else {
                Pair(column, row)
            }
        } else {
            findFirstEmptySpot(items) ?: return
        }
        
        items.add(HomeItem(packageName, finalCol, finalRow))
        pages[pageIndex] = currentPage.copy(items = items)
        
        val newLayout = currentLayout.copy(pages = pages)
        state.layout = newLayout
        repository.save(newLayout)
    }
    
    private fun findFirstEmptySpot(items: List<HomeItem>): Pair<Int, Int>? {
        // Search in a 4x7 grid (default adaptive max)
        for (r in 0 until 7) {
            for (c in 0 until 6) {
                if (items.none { it.column == c && it.row == r }) {
                    return Pair(c, r)
                }
            }
        }
        return null
    }
    
    fun moveOnHome(packageName: String, pageIndex: Int = 0, targetColumn: Int, targetRow: Int) {
        val currentLayout = state.layout
        val pages = currentLayout.pages.toMutableList()
        if (pageIndex !in pages.indices) return
        
        val page = pages[pageIndex]
        val items = page.items.toMutableList()
        
        val itemIndex = items.indexOfFirst { it.packageName == packageName }
        if (itemIndex == -1) return
        
        val oldItem = items[itemIndex]
        if (oldItem.column == targetColumn && oldItem.row == targetRow) return
        
        val occupyingIndex = items.indexOfFirst { it.column == targetColumn && it.row == targetRow }
        
        if (occupyingIndex != -1) {
            val occupyingItem = items[occupyingIndex]
            items[itemIndex] = occupyingItem.copy(column = oldItem.column, row = oldItem.row)
            items[occupyingIndex] = oldItem.copy(column = targetColumn, row = targetRow)
        } else {
            items[itemIndex] = oldItem.copy(column = targetColumn, row = targetRow)
        }
        
        pages[pageIndex] = page.copy(items = items)
        val newLayout = currentLayout.copy(pages = pages)
        state.layout = newLayout
        repository.save(newLayout)
    }

    fun removeFromHome(packageName: String) {
        val currentLayout = state.layout
        val pages = currentLayout.pages.map { page ->
            page.copy(items = page.items.filter { it.packageName != packageName })
        }
        val newLayout = currentLayout.copy(pages = pages)
        state.layout = newLayout
        repository.save(newLayout)
    }
    
    fun isOnHome(packageName: String): Boolean {
        return state.layout.pages.any { page ->
            page.items.any { it.packageName == packageName }
        }
    }

    fun addToDock(packageName: String) {
        val currentLayout = state.layout
        val dock = currentLayout.dockPackages.toMutableList()
        if (dock.contains(packageName)) return
        if (dock.size >= 8) return // Limit dock items

        dock.add(packageName)
        val newLayout = currentLayout.copy(dockPackages = dock)
        state.layout = newLayout
        repository.save(newLayout)
    }

    fun removeFromDock(packageName: String) {
        val currentLayout = state.layout
        val dock = currentLayout.dockPackages.filter { it != packageName }
        val newLayout = currentLayout.copy(dockPackages = dock)
        state.layout = newLayout
        repository.save(newLayout)
    }

    fun isOnDock(packageName: String): Boolean {
        return state.layout.dockPackages.contains(packageName)
    }
}

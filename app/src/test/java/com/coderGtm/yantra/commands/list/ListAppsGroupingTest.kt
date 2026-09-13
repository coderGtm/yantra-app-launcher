package com.coderGtm.yantra.commands.list

import org.junit.Assert.*
import org.junit.Test

class ListAppsGroupingTest {

    @Test
    fun `app categories include play store names plus Other last`() {
        assertTrue(AppCategories.ALL.contains("Social"))
        assertTrue(AppCategories.ALL.contains("Games"))
        assertEquals("Other", AppCategories.ALL.last())
    }

    @Test
    fun `group apps sorts categories A-Z with Other last and apps A-Z inside`() {
        val input = listOf(
            CategorizedApp("Zeta", "com.example.zeta", "Social"),
            CategorizedApp("Alpha", "com.example.alpha", "Social"),
            CategorizedApp("Solo", "com.example.solo", "Other"),
            CategorizedApp("GameOne", "com.example.game", "Games"),
        )
        val grouped = groupAppsByCategory(input)
        assertEquals(listOf("Games", "Social", "Other"), grouped.map { it.category })
        assertEquals(listOf("Alpha", "Zeta"), grouped.first { it.category == "Social" }.apps.map { it.appName })
    }

    @Test
    fun `normalize category filter is case-insensitive and rejects unknown`() {
        assertEquals("Social", normalizeCategoryFilter("social"))
        assertEquals("Games", normalizeCategoryFilter("GAMES"))
        assertNull(normalizeCategoryFilter("notacategory"))
    }
}

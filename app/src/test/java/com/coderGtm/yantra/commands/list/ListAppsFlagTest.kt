package com.coderGtm.yantra.commands.list

import org.junit.Assert.*
import org.junit.Test

class ListAppsFlagTest {

    @Test
    fun `empty args show names only without filter`() {
        val result = parseListAppsArgs(emptyList())
        assertTrue(result is ListAppsArgs.Valid)
        val valid = result as ListAppsArgs.Valid
        assertNull(valid.filter)
        assertFalse(valid.showPackages)
    }

    @Test
    fun `dash p alone shows packages without filter`() {
        val result = parseListAppsArgs(listOf("-p")) as ListAppsArgs.Valid
        assertNull(result.filter)
        assertTrue(result.showPackages)
    }

    @Test
    fun `category alone filters without packages`() {
        val result = parseListAppsArgs(listOf("social")) as ListAppsArgs.Valid
        assertEquals("Social", result.filter)
        assertFalse(result.showPackages)
    }

    @Test
    fun `category plus trailing dash p filters with packages`() {
        val result = parseListAppsArgs(listOf("Social", "-p")) as ListAppsArgs.Valid
        assertEquals("Social", result.filter)
        assertTrue(result.showPackages)
    }

    @Test
    fun `dash p first is invalid`() {
        assertTrue(parseListAppsArgs(listOf("-p", "Social")) is ListAppsArgs.Invalid)
    }

    @Test
    fun `unknown category is invalid`() {
        assertTrue(parseListAppsArgs(listOf("notacat")) is ListAppsArgs.Invalid)
    }

    @Test
    fun `format app line hides package by default`() {
        val app = CategorizedApp("Gmail", "com.google.gmail", "Social")
        assertEquals("- Gmail", formatAppLine(app, showPackages = false))
    }

    @Test
    fun `format app line shows package with flag`() {
        val app = CategorizedApp("Gmail", "com.google.gmail", "Social")
        assertEquals("- Gmail (com.google.gmail)", formatAppLine(app, showPackages = true))
    }

    @Test
    fun `format category title has no dashes`() {
        val group = CategoryGroup("Social", emptyList())
        assertEquals("Social (5)", formatCategoryTitle(group, 5))
        assertFalse(formatCategoryTitle(group, 5).contains("─"))
        assertFalse(formatCategoryTitle(group, 5).contains("-"))
    }
}

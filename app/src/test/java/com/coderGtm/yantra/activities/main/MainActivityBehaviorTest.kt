package com.coderGtm.yantra.activities.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class MainActivityBehaviorTest {

    @Test
    fun `shouldHandleCommand rejects blank commands`() {
        assertFalse(MainActivityBehavior.shouldHandleCommand(null))
        assertFalse(MainActivityBehavior.shouldHandleCommand(""))
        assertFalse(MainActivityBehavior.shouldHandleCommand("   "))
    }

    @Test
    fun `shouldHandleCommand accepts non blank commands`() {
        assertTrue(MainActivityBehavior.shouldHandleCommand("lock"))
    }

    @Test
    fun `shouldHandleSwipeCommand requires pro and non blank command`() {
        assertTrue(MainActivityBehavior.shouldHandleSwipeCommand(true, "open"))
        assertFalse(MainActivityBehavior.shouldHandleSwipeCommand(false, "open"))
        assertFalse(MainActivityBehavior.shouldHandleSwipeCommand(true, "  "))
    }

    @Test
    fun `shouldRunInit runs when initialized pro and not already ran`() {
        assertTrue(MainActivityBehavior.shouldRunInit(initialized = true, isPro = true, alreadyRanForThisStart = false))
    }

    @Test
    fun `shouldRunInit skips when terminal not initialized`() {
        assertFalse(MainActivityBehavior.shouldRunInit(initialized = false, isPro = true, alreadyRanForThisStart = false))
    }

    @Test
    fun `shouldRunInit skips second run for same start`() {
        assertFalse(MainActivityBehavior.shouldRunInit(initialized = true, isPro = true, alreadyRanForThisStart = true))
    }

    @Test
    fun `shouldRunInit skips for non pro users`() {
        assertFalse(MainActivityBehavior.shouldRunInit(initialized = true, isPro = false, alreadyRanForThisStart = false))
    }

    @Test
    fun `shouldRunInit runs again after reset for next start`() {
        // every-onStart semantics: onStop clears the flag, next onStart may run again
        assertFalse(MainActivityBehavior.shouldRunInit(initialized = true, isPro = true, alreadyRanForThisStart = true))
        assertTrue(MainActivityBehavior.shouldRunInit(initialized = true, isPro = true, alreadyRanForThisStart = false))
    }

    @Test
    fun `buildBackupFileName follows expected format`() {
        val fileName = MainActivityBehavior.buildBackupFileName(Date(0))
        assertTrue(fileName.startsWith("backup_"))
        assertTrue(fileName.endsWith(".yantra"))
        assertTrue(fileName.matches(Regex("backup_\\d{4}_\\d{2}_\\d{2}_\\d{4}\\.yantra")))
    }
}

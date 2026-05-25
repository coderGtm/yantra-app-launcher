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
    fun `buildBackupFileName follows expected format`() {
        val fileName = MainActivityBehavior.buildBackupFileName(Date(0))
        assertTrue(fileName.startsWith("backup_"))
        assertTrue(fileName.endsWith(".yantra"))
        assertTrue(fileName.matches(Regex("backup_\\d{4}_\\d{2}_\\d{2}_\\d{4}\\.yantra")))
    }
}

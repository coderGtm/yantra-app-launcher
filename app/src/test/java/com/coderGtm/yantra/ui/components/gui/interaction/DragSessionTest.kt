package com.coderGtm.yantra.ui.components.gui.interaction

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DragSessionTest {

    @Test
    fun testDragSessionProperties() {
        val session = DragSession(
            itemId = "com.example.test",
            source = DragSource.DRAWER,
            startPosition = Offset(10f, 20f),
            currentPosition = Offset(10f, 20f),
            hasMoved = false
        )

        assertEquals("com.example.test", session.itemId)
        assertEquals(DragSource.DRAWER, session.source)
        assertFalse(session.hasMoved)

        val updated = session.copy(currentPosition = Offset(50f, 60f), hasMoved = true)
        assertTrue(updated.hasMoved)
        assertEquals(Offset(50f, 60f), updated.currentPosition)
    }
}

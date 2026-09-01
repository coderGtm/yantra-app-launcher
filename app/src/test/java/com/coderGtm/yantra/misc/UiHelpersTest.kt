package com.coderGtm.yantra.misc

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyImeHeightTest {

    @Test
    fun `no keyboard with gesture nav gives zero`() {
        assertEquals(0, legacyImeHeightFromFrame(viewHeight = 1000, visibleBottom = 1000, navBarBottom = 0))
    }

    @Test
    fun `no keyboard with three button nav gives zero`() {
        assertEquals(0, legacyImeHeightFromFrame(viewHeight = 1000, visibleBottom = 900, navBarBottom = 100))
    }

    @Test
    fun `keyboard with gesture nav gives keyboard height`() {
        assertEquals(500, legacyImeHeightFromFrame(viewHeight = 1000, visibleBottom = 500, navBarBottom = 0))
    }

    @Test
    fun `keyboard with three button nav isolates keyboard height`() {
        assertEquals(500, legacyImeHeightFromFrame(viewHeight = 1000, visibleBottom = 450, navBarBottom = 50))
    }

    @Test
    fun `small transient delta is ignored as noise`() {
        assertEquals(0, legacyImeHeightFromFrame(viewHeight = 1000, visibleBottom = 950, navBarBottom = 0))
    }

    @Test
    fun `delta below one quarter of view is ignored`() {
        assertEquals(0, legacyImeHeightFromFrame(viewHeight = 800, visibleBottom = 650, navBarBottom = 0))
    }
}
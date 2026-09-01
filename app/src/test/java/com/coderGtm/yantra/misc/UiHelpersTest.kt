package com.coderGtm.yantra.misc

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyImeResizeTest {

    @Test
    fun `pre-30 devices require adjustResize`() {
        assertEquals(true, requiresLegacyImeResize(23))
        assertEquals(true, requiresLegacyImeResize(29))
    }

    @Test
    fun `android 11 and above keep insets-based handling`() {
        assertEquals(false, requiresLegacyImeResize(30))
        assertEquals(false, requiresLegacyImeResize(36))
    }
}
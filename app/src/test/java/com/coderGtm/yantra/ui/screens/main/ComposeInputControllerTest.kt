package com.coderGtm.yantra.ui.screens.main

import org.junit.Assert.assertEquals
import org.junit.Test

class ComposeInputControllerTest {

    @Test
    fun `pre-attach setText buffers text and notifies listeners`() {
        val controller = ComposeInputController()
        val seen = mutableListOf<String>()
        controller.addTextChangedListener { seen += it.toString() }

        controller.setText("help")

        assertEquals("help", controller.text.toString())
        assertEquals("help", controller.inputText)
        assertEquals(listOf("help"), seen)
    }

    @Test
    fun `pre-attach clear notifies empty text`() {
        val controller = ComposeInputController()
        val seen = mutableListOf<String>()
        controller.addTextChangedListener { seen += it.toString() }

        controller.setText("help")
        controller.setText("")

        assertEquals("", controller.text.toString())
        assertEquals("", controller.inputText)
        assertEquals(listOf("help", ""), seen)
    }

    @Test
    fun `pre-attach setSelection clamps to buffered text`() {
        val controller = ComposeInputController()
        controller.setText("help")

        // No crash, and stays valid; view applies it on attach.
        controller.setSelection(99)
        controller.setText("ls")
        assertEquals("ls", controller.text.toString())
    }

    @Test
    fun `style state is retained for attach`() {
        val controller = ComposeInputController()
        controller.setTextColor(0xFFFF0000.toInt())
        controller.textSize = 20f
        controller.isEnabled = false

        assertEquals(0xFFFF0000.toInt(), controller.textColorInt)
        assertEquals(20f, controller.textSize)
        assertEquals(false, controller.isEnabled)
    }
}

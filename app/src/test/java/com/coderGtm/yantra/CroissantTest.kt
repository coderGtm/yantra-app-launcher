package com.coderGtm.yantra

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import org.json.JSONArray
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CroissantTest {

    @Test
    fun `hasDataError detects plain error sentinel`() {
        val jsonArray = JSONArray(arrayOf("Error while getting data!"))
        assertTrue(Croissant().hasDataError(jsonArray))
    }

    @Test
    fun `hasDataError detects error sentinel with appended exception message`() {
        val jsonArray = JSONArray(arrayOf("Error while getting data!SecurityException: Permission Denial"))
        assertTrue(Croissant().hasDataError(jsonArray))
    }

    @Test
    fun `hasDataError returns false for json object response`() {
        val jsonArray = JSONArray("[{\"error\":\"Some error\"}]")
        assertFalse(Croissant().hasDataError(jsonArray))
    }

    @Test
    fun `hasDataError returns false for valid file list response`() {
        val jsonArray = JSONArray("[{\"name\":\"file\",\"type\":false,\"visibility\":false}]")
        assertFalse(Croissant().hasDataError(jsonArray))
    }

    @Test
    fun `hasDataError returns false for empty array`() {
        val jsonArray = JSONArray()
        assertFalse(Croissant().hasDataError(jsonArray))
    }

    @Test
    fun `checkCroissantPermission returns false without crashing when provider returns data error`() {
        val activity = mock<Activity>()
        val resolver = mock<ContentResolver>()
        val cursor = mock<Cursor>()
        val uri = mock<Uri>()
        val builder = mock<Uri.Builder>()
        whenever(activity.contentResolver).thenReturn(resolver)
        whenever(resolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
        whenever(cursor.moveToFirst()).thenReturn(true)
        whenever(cursor.getColumnIndex("response")).thenReturn(0)
        whenever(cursor.getString(0)).thenReturn("[\"Error while getting data!\"]")
        whenever(uri.buildUpon()).thenReturn(builder)
        whenever(builder.appendQueryParameter(any<String>(), any<String>())).thenReturn(builder)
        whenever(builder.build()).thenReturn(uri)

        val mockedUri = Mockito.mockStatic(Uri::class.java)
        mockedUri.use {
            it.`when`<Uri> { Uri.parse(any<String>()) }.thenReturn(uri)
            assertFalse(Croissant().checkCroissantPermission(activity))
        }
    }
}
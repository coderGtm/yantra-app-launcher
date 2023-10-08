package com.coderGtm.yantra.utils

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ScrollView

interface TerminalGestureListenerCallback {
    fun onSingleTap()
    fun onDoubleTap()
}

class TerminalScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {

    private val gestureDetector: GestureDetector = GestureDetector(context, MyGestureListener())
    private var callback: TerminalGestureListenerCallback? = null

    fun setGestureListenerCallback(callback: TerminalGestureListenerCallback) {
        this.callback = callback
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(ev) || super.onTouchEvent(ev)
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            callback?.onSingleTap()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            callback?.onDoubleTap()
            return true
        }
    }
}
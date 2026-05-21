package com.coderGtm.yantra.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ScrollView
import kotlin.math.abs


interface TerminalGestureListenerCallback {
    fun onSingleTap()
    fun onDoubleTap()
    fun onSwipeRight()
    fun onSwipeLeft()
}

class TerminalScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs) {

    private val gestureDetector: GestureDetector = GestureDetector(context, MyGestureListener())
    private var callback: TerminalGestureListenerCallback? = null

    fun setGestureListenerCallback(callback: TerminalGestureListenerCallback) {
        this.callback = callback
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(ev) || gestureDetector.onTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(ev) || super.onTouchEvent(ev)
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        private val swipeThreshold = 250
        private val swipeVelocityThreshold = 100

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            callback?.onSingleTap()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            callback?.onDoubleTap()
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - (e1?.y ?: return false)
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                        if (diffX > 0) {
                            callback?.onSwipeRight()
                        } else {
                            callback?.onSwipeLeft()
                        }
                        result = true
                    }
                } else {
                    // Handle vertical swipe
                    if (abs(diffY) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {
                        // if at top and swiping down, open notification panel
                        if (diffY > 0 && isAtTop()) {
                            // Open notification panel
                           expandNotificationPanel(context)
                            return true
                        } else {
                            // Let ScrollView handle the vertical swipe
                            return false
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }
    }
    fun scrollToBottom() {
        val lastChild = this.getChildAt(this.childCount - 1)
        val bottom = lastChild.bottom + this.paddingBottom
        val delta = bottom - (this.scrollY+ this.height)
        this.smoothScrollBy(0, delta)
    }

    fun isAtTop(): Boolean {
        return this.scrollY == 0
    }

    fun expandNotificationPanel(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")

            val method =
                statusBarManager.getMethod("expandNotificationsPanel")

            method.invoke(statusBarService)
        } catch (_: Exception) {}
    }

}
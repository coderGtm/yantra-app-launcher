package com.coderGtm.yantra.terminal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText


class TerminalEditText : AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private val inlineCompletionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var inlineCompletionSuffix: String? = null
    private var inlineCompletionColor: Int = Color.GRAY

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val conn = super.onCreateInputConnection(outAttrs)
        outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()
        return conn
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawInlineCompletion(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && isInlineCompletionTouched(event.x, event.y)) {
            acceptInlineCompletion()
            performClick()
            return true
        }

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setInlineCompletion(suffix: String?, color: Int = currentTextColor) {
        val normalizedSuffix = suffix?.takeIf { it.isNotEmpty() }
        if (inlineCompletionSuffix == normalizedSuffix && inlineCompletionColor == color) {
            return
        }

        inlineCompletionSuffix = normalizedSuffix
        inlineCompletionColor = color
        invalidate()
    }

    private fun drawInlineCompletion(canvas: Canvas) {
        val suffix = inlineCompletionSuffix ?: return
        val drawPoint = getInlineCompletionDrawPoint() ?: return

        configureInlineCompletionPaint()
        canvas.drawText(suffix, drawPoint.x, drawPoint.baseline, inlineCompletionPaint)
    }

    private fun isInlineCompletionTouched(x: Float, y: Float): Boolean {
        val suffix = inlineCompletionSuffix ?: return false
        val drawPoint = getInlineCompletionDrawPoint() ?: return false

        configureInlineCompletionPaint()
        val touchPadding = 12f * resources.displayMetrics.density
        val lineHeight = lineHeight.toFloat()
        val suffixWidth = inlineCompletionPaint.measureText(suffix)

        return x >= drawPoint.x - touchPadding &&
            x <= drawPoint.x + suffixWidth + touchPadding &&
            y >= drawPoint.baseline - lineHeight &&
            y <= drawPoint.baseline + touchPadding
    }

    private fun acceptInlineCompletion() {
        val suffix = inlineCompletionSuffix ?: return
        val editable = text ?: return
        editable.append(suffix)
        setSelection(editable.length)
        setInlineCompletion(null)
    }

    private fun getInlineCompletionDrawPoint(): InlineCompletionDrawPoint? {
        val editable = text ?: return null
        val textLayout = layout ?: return null
        val cursorPosition = selectionStart

        if (!hasFocus() || cursorPosition != selectionEnd || cursorPosition != editable.length) {
            return null
        }

        val line = textLayout.getLineForOffset(cursorPosition)
        val x = compoundPaddingLeft + textLayout.getPrimaryHorizontal(cursorPosition) - scrollX
        val baseline = extendedPaddingTop + textLayout.getLineBaseline(line).toFloat() - scrollY

        return InlineCompletionDrawPoint(x, baseline)
    }

    private fun configureInlineCompletionPaint() {
        inlineCompletionPaint.set(paint)
        inlineCompletionPaint.color = inlineCompletionColor
        inlineCompletionPaint.alpha = INLINE_COMPLETION_ALPHA
        inlineCompletionPaint.style = Paint.Style.FILL
    }

    private data class InlineCompletionDrawPoint(
        val x: Float,
        val baseline: Float
    )

    companion object {
        private const val INLINE_COMPLETION_ALPHA = 95
    }
}

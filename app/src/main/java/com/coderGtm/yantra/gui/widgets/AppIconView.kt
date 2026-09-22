package com.coderGtm.yantra.gui.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.drawable.Drawable

class AppIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val labelView: TextView

    init {

        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        val container = LinearLayout(context).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

        }

        iconView = ImageView(context).apply {

            layoutParams = LinearLayout.LayoutParams(
                dp(56),
                dp(56)
            )

            scaleType = ImageView.ScaleType.FIT_CENTER

            adjustViewBounds = true

        }

        labelView = TextView(context).apply {

            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )

            gravity = Gravity.CENTER

            textSize = 12f

            setTextColor(Color.WHITE)

            setTypeface(typeface, Typeface.BOLD)

            text = "Application"

        }

        container.addView(iconView)
        container.addView(labelView)

        addView(container)

    }

    fun setTitle(title: String) {

        labelView.text = title

    }

    fun setIcon(drawable: Drawable?) {

        if (drawable != null) {
            iconView.setImageDrawable(drawable)
        } else {
            iconView.setImageResource(android.R.drawable.sym_def_app_icon)
        }

    }

    fun setOnIconClick(action: () -> Unit) {

        setOnClickListener {

            action()

        }

    }

    private fun dp(value: Int): Int {

        return (value * resources.displayMetrics.density).toInt()

    }

}
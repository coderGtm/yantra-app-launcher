package com.coderGtm.yantra.misc

import android.content.Context
import android.widget.TextView
import com.coderGtm.yantra.R
import com.skydoves.colorpickerview.AlphaTileView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.flag.FlagView

class CustomFlag(context: Context?, layout: Int) : FlagView(context, layout) {
    private val textView: TextView = findViewById(R.id.flag_color_code)
    private val alphaTileView: AlphaTileView = findViewById(R.id.flag_color_layout)

    override fun onRefresh(colorEnvelope: ColorEnvelope) {
        textView.text = "#${colorEnvelope.hexCode.substring(2)}"
        alphaTileView.setPaintColor(colorEnvelope.color)
    }

    override fun onFlipped(isFlipped: Boolean?) {
        if (isFlipped == true) {
            textView.rotation = 180f;
        } else {
            textView.rotation = 0f;
        }

    }
}
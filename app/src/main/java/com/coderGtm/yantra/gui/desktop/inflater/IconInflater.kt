package com.coderGtm.yantra.gui.desktop.inflater

import android.content.Context
import com.coderGtm.yantra.gui.desktop.model.DesktopItem
import com.coderGtm.yantra.gui.widgets.AppIconView

object IconInflater {

    fun inflate(
        context: Context,
        item: DesktopItem
    ): AppIconView {

        val view = AppIconView(context)

        view.setTitle(item.label)

        view.setIcon(item.icon)

        item.packageName?.let { packageName ->

            view.setOnIconClick {

                val intent =
                    context.packageManager
                        .getLaunchIntentForPackage(packageName)

                intent?.let {

                    context.startActivity(it)

                }

            }

        }

        return view
    }
}
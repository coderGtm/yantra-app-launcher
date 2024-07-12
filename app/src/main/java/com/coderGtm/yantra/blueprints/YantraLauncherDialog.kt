package com.coderGtm.yantra.blueprints

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.coderGtm.yantra.R
import com.google.android.material.button.MaterialButton

class YantraLauncherDialog(val context: Context) {
    fun showInfo(
        title: String,
        message: String,
        positiveButton: String,
        negativeButton: String,
        positiveAction: () -> Unit,
        negativeAction: () -> Unit
    ) {
        // Show dialog with the given title, message, positiveButton, negativeButton
        // and perform the actions when the buttons are clicked
        // use new_dialog_info.xml layout for the dialog
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.yantra_launcher_dialog_container)

        val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.90).toInt()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val dialogTitle: TextView = dialog.findViewById(R.id.titleText)
        val dialogBody: TextView = dialog.findViewById(R.id.bodyText)
        val dialogPositiveButton: MaterialButton = dialog.findViewById(R.id.positiveButton)
        val dialogNegativeButton: MaterialButton = dialog.findViewById(R.id.negativeButton)
        val closeButton: ImageButton = dialog.findViewById(R.id.closeButton)

        dialogTitle.text = title
        dialogBody.text = message
        dialogPositiveButton.text = positiveButton
        dialogNegativeButton.text = negativeButton

        dialogPositiveButton.setOnClickListener {
            positiveAction()
            dialog.dismiss()
        }
        dialogNegativeButton.setOnClickListener {
            negativeAction()
            dialog.dismiss()
        }
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        if (positiveButton.isEmpty()) {
            dialogPositiveButton.visibility = MaterialButton.GONE
        }
        if (negativeButton.isEmpty()) {
            dialogNegativeButton.visibility = MaterialButton.GONE
        }

        dialog.show()
    }

}
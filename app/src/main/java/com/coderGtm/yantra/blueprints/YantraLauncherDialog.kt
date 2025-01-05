package com.coderGtm.yantra.blueprints

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.alpha
import androidx.core.graphics.drawable.toDrawable
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import com.coderGtm.yantra.dpToPx
import com.coderGtm.yantra.getCurrentTheme
import com.google.android.material.button.MaterialButton

class YantraLauncherDialog(val context: Context) {
    val theme = getCurrentTheme(context as Activity, context.getSharedPreferences(
        SHARED_PREFS_FILE_NAME,0))
    private var textColor: Int = 0
    private var bodyTextColor: Int = 0
    private val dialogBgColor = FloatArray(3)
    private val dialogBorderColor = FloatArray(3)
    private val positiveColor = theme.successTextColor
    private val negativeColor = theme.errorTextColor
    init {
        Color.colorToHSV(theme.bgColor, dialogBgColor)
        Color.colorToHSV(theme.bgColor, dialogBorderColor)

        textColor = if (dialogBgColor[2] > 0.5) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        // set body text color as 75% opacity of textColor
        bodyTextColor = Color.argb(191, Color.red(textColor), Color.green(textColor), Color.blue(textColor))
        if (dialogBgColor[2] >= 0.15) {
            dialogBgColor[2] -= 0.1f
        } else if (dialogBgColor[2] <= 0.15) {
            dialogBorderColor[2] += 0.1f
        }
    }



    fun showInfo(
        title: String,
        message: String,
        cancellable: Boolean = true,
        positiveButton: String,
        negativeButton: String = "",
        positiveAction: () -> Unit = {},
        negativeAction: () -> Unit = {},
        dismissAction: () -> Unit = {}
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

        val dialogContainer: ConstraintLayout = dialog.findViewById(R.id.dialogContainer)
        val dialogTitle: TextView = dialog.findViewById(R.id.titleText)
        val dialogBodyContainer = dialog.findViewById<ViewGroup>(R.id.bodyContainer)
        val dialogBody: TextView = dialog.findViewById(R.id.bodyText)
        val dialogInput: EditText = dialog.findViewById(R.id.input)
        val dialogPositiveButton: MaterialButton = dialog.findViewById(R.id.positiveButton)
        val dialogNegativeButton: MaterialButton = dialog.findViewById(R.id.negativeButton)
        val closeButton: ImageButton = dialog.findViewById(R.id.closeButton)

        dialogInput.visibility = EditText.GONE
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
        dialog.setOnDismissListener {
            dismissAction()
        }

        if (positiveButton.isEmpty()) {
            dialogPositiveButton.visibility = MaterialButton.GONE
        }
        if (negativeButton.isEmpty()) {
            dialogNegativeButton.visibility = MaterialButton.GONE
        }

        dialogContainer.backgroundTintList = ColorStateList.valueOf(Color.HSVToColor(dialogBorderColor))
        dialogBodyContainer.backgroundTintList = ColorStateList.valueOf(Color.HSVToColor(dialogBgColor))
        dialogTitle.setTextColor(textColor)
        dialogBody.setTextColor(bodyTextColor)
        dialogPositiveButton.backgroundTintList = ColorStateList.valueOf(positiveColor)
        dialogNegativeButton.backgroundTintList = ColorStateList.valueOf(negativeColor)
        closeButton.imageTintList = ColorStateList.valueOf(negativeColor)
        dialogPositiveButton.setTextColor(Color.HSVToColor(dialogBgColor))
        dialogNegativeButton.setTextColor(Color.HSVToColor(dialogBgColor))

        dialog.setCancelable(cancellable)
        dialog.show()
    }

    fun takeInput(
        title: String,
        message: String,
        cancellable: Boolean = true,
        positiveButton: String,
        negativeButton: String = "",
        positiveAction: (String) -> Unit = {},
        negativeAction: () -> Unit = {},
        dismissAction: () -> Unit = {},
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        initialInput: String = ""
    ) {
        // Show dialog with the given title, message, positiveButton, negativeButton
        // and perform the actions when the buttons are clicked
        // use new_dialog_input.xml layout for the dialog
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.yantra_launcher_dialog_container)

        val width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (context.resources.displayMetrics.heightPixels * 0.90).toInt()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val dialogContainer: ConstraintLayout = dialog.findViewById(R.id.dialogContainer)
        val dialogTitle: TextView = dialog.findViewById(R.id.titleText)
        val dialogBodyContainer = dialog.findViewById<ViewGroup>(R.id.bodyContainer)
        val dialogBody: TextView = dialog.findViewById(R.id.bodyText)
        val dialogInput: EditText = dialog.findViewById(R.id.input)
        val dialogPositiveButton: MaterialButton = dialog.findViewById(R.id.positiveButton)
        val dialogNegativeButton: MaterialButton = dialog.findViewById(R.id.negativeButton)
        val closeButton: ImageButton = dialog.findViewById(R.id.closeButton)

        dialogTitle.text = title
        dialogBody.text = message
        dialogInput.setText(initialInput)
        dialogPositiveButton.text = positiveButton
        dialogNegativeButton.text = negativeButton
        dialogInput.inputType = inputType
        if (inputType == InputType.TYPE_TEXT_FLAG_MULTI_LINE) {
            dialogInput.isSingleLine = false
            dialogInput.setLines(10)
        }

        dialogPositiveButton.setOnClickListener {
            positiveAction(dialogInput.text.toString())
            dialog.dismiss()
        }
        dialogNegativeButton.setOnClickListener {
            negativeAction()
            dialog.dismiss()
        }
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setOnDismissListener {
            dismissAction()
        }

        if (positiveButton.isEmpty()) {
            dialogPositiveButton.visibility = MaterialButton.GONE
        }
        if (negativeButton.isEmpty()) {
            dialogNegativeButton.visibility = MaterialButton.GONE
        }

        val inputShape = GradientDrawable()
        inputShape.shape = GradientDrawable.RECTANGLE
        inputShape.setColor(Color.HSVToColor(dialogBgColor))
        inputShape.setStroke(dpToPx(1, context), textColor)
        val inputShapeCornerRadii = dpToPx(25, context).toFloat()
        val inputShapePadding = dpToPx(10, context)
        inputShape.cornerRadii = floatArrayOf(inputShapeCornerRadii, inputShapeCornerRadii, inputShapeCornerRadii, inputShapeCornerRadii, inputShapeCornerRadii, inputShapeCornerRadii, inputShapeCornerRadii, inputShapeCornerRadii)

        dialogContainer.backgroundTintList = ColorStateList.valueOf(Color.HSVToColor(dialogBorderColor))
        dialogBodyContainer.backgroundTintList = ColorStateList.valueOf(Color.HSVToColor(dialogBgColor))
        dialogTitle.setTextColor(textColor)
        dialogBody.setTextColor(bodyTextColor)
        dialogInput.background = inputShape
        dialogInput.setPadding(inputShapePadding, inputShapePadding, inputShapePadding, inputShapePadding)
        dialogInput.setTextColor(textColor)

        dialogPositiveButton.backgroundTintList = ColorStateList.valueOf(positiveColor)
        dialogNegativeButton.backgroundTintList = ColorStateList.valueOf(negativeColor)
        closeButton.imageTintList = ColorStateList.valueOf(negativeColor)
        dialogPositiveButton.setTextColor(Color.HSVToColor(dialogBgColor))
        dialogNegativeButton.setTextColor(Color.HSVToColor(dialogBgColor))

        dialog.setCancelable(cancellable)
        dialog.show()
    }
}
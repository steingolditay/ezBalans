package com.ezbalans.app.ezbalans.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup

class CustomDialogObject{

    companion object {

        fun create(context: Context, layout: Int): Dialog{
            val dialog = Dialog(context)
            dialog.setContentView(layout)
            val window = dialog.window!!
            val attributes = window.attributes
//            attributes.gravity = Gravity.BOTTOM
            attributes.dimAmount = 0.3f
            window.attributes = attributes
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            return dialog

        }
    }
}
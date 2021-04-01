package com.ezbalans.app.ezbalans.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.ezbalans.app.ezbalans.R

class GetLoadingDialog (context: Context, private val input: String?) {

    val dialog = Dialog(context)

    fun create(): Dialog{

        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)

        val body = dialog.findViewById<TextView>(R.id.body)
        body.text = input

        val window = dialog.window!!
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.dimAmount = 0.2f
        window.attributes = attributes
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.attributes.windowAnimations = R.style.DialogAnimation

        return dialog

    }


}
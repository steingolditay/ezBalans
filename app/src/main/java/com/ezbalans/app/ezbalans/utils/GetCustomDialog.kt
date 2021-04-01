package com.ezbalans.app.ezbalans.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import com.ezbalans.app.ezbalans.R

class GetCustomDialog (dialog: Dialog, layout: Int) {

    private val contextDialog = dialog
    private val contextLayout = layout

    fun create(): Dialog{

        contextDialog.setContentView(contextLayout)
        val window = contextDialog.window!!
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.dimAmount = 0.2f
        window.attributes = attributes
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.attributes.windowAnimations = R.style.DialogAnimation

        return contextDialog


    }

}
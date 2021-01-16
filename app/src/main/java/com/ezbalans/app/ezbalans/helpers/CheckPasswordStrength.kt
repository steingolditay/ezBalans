package com.ezbalans.app.ezbalans.helpers

import android.content.Context
import com.ezbalans.app.ezbalans.R

class CheckPasswordStrength {

    fun check(context: Context, input: String): String{
        return when  {
            input.length < 6 -> {
                context.getString(R.string.pass_size_error)
            }
            !hasDigit(input) -> {
                context.getString(R.string.pass_digit_error)
            }
            !hasUpperCase(input) -> {
                context.getString(R.string.pass_uppercase_error)
            }
            !hasLowerCase(input) -> {
                context.getString(R.string.pass_lowercase_error)
            }
            !hasSpecialChar(input) -> {
                context.getString(R.string.pass_special_char_error)
            }
            else -> {
                ""
            }
        }
    }

    private fun hasDigit(input: String): Boolean {
        for (c in input){
            if (c.isDigit()){
                return true
            }
        }
        return false
    }

    private fun hasUpperCase(input: String): Boolean {
        for (c in input){
            if (c.isUpperCase()){
                return true
            }
        }
        return false
    }

    private fun hasLowerCase(input: String): Boolean {
        for (c in input){
            if (c.isLowerCase()){
                return true
            }
        }
        return false
    }

    private fun hasSpecialChar(input: String): Boolean {
        val specialChars = arrayOf("!", "@", "#", "$", "%", "^", "&", "*")
        for (c in input){
            if (specialChars.contains(c.toString())){
                return true
            }
        }
        return false
    }
}
package com.ezbalans.app.ezbalans.helpers

class GetIdentityKey {

    fun create(): String{
        val allowedChars = ('A'..'Z') + ('0'..'9')

        return (1..6).map{allowedChars.random()}.joinToString("")
    }
}
package com.ezbalans.app.ezbalans.helpers

class GetIdentityKey {

    fun create(keyList: List<String>): String{
        val allowedChars = ('A'..'Z') + ('0'..'9')
        val key = (1..6).map{allowedChars.random()}.joinToString("")
        if (key in keyList){
            return create(keyList)
        }
        return key
    }
}
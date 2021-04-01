package com.ezbalans.app.ezbalans.utils

import android.content.Context
import android.content.res.Configuration
import com.preference.PowerPreference
import java.util.*

object LocaleManager {
    fun setLocale(context: Context): Context{
        checkLanguage()
        val lang = PowerPreference.getDefaultFile().getString(Constants.language)

        val languageCode = if (lang == Constants.language_hebrew) "iw" else "eng"
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config: Configuration = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    private fun checkLanguage(){
        val lang = PowerPreference.getDefaultFile().getString(Constants.language)
        if (lang.isEmpty()) {
            val currentDeviceLanguage = Locale.getDefault().isO3Language
            if (currentDeviceLanguage == "he" || currentDeviceLanguage == "heb") {
                PowerPreference.getDefaultFile().putString(
                    Constants.language,
                    Constants.language_hebrew
                )
            }
            else {
                PowerPreference.getDefaultFile().putString(
                    Constants.language,
                    Constants.language_english
                )
            }
        }
    }
}
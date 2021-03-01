package com.ezbalans.app.ezbalans.views

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.helpers.Constants
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.views.signIn.WelcomeActivity
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.preference.PowerPreference

class AppIntro: AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_welcome))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_home))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_rooms))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_room))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_budget))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_goodbye))

    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)

        PowerPreference.getDefaultFile().putBoolean(Constants.first_time, false)
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        PowerPreference.getDefaultFile().putBoolean(Constants.first_time, false)
        super.onDonePressed(currentFragment)
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
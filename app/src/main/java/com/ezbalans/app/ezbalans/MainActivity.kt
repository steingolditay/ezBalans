package com.ezbalans.app.ezbalans

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.databinding.ViewSplashScreenBinding
import com.ezbalans.app.ezbalans.utils.Constants
import com.ezbalans.app.ezbalans.utils.LocaleManager
import com.ezbalans.app.ezbalans.views.AppIntro
import com.ezbalans.app.ezbalans.views.HomeActivity
import com.ezbalans.app.ezbalans.views.signIn.WelcomeActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ViewSplashScreenBinding


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewSplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PowerPreference.init(this)
        val firebaseUser = Firebase.auth.currentUser

        when {
            // go to app intro
            PowerPreference.getDefaultFile().getBoolean(Constants.first_time, true) -> {
                val intent = Intent(this, AppIntro::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

            // go to home activity
            firebaseUser != null -> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

            // go to signin activity
            else -> {
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }


    }
}
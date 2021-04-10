package com.ezbalans.app.ezbalans

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.databinding.ViewSplashScreenBinding
import com.ezbalans.app.ezbalans.utils.LocaleManager
import com.ezbalans.app.ezbalans.viewmodels.roomActivities.RoomActivityViewModel
import com.ezbalans.app.ezbalans.presentation.HomeActivity
import com.ezbalans.app.ezbalans.presentation.signIn.WelcomeActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ViewSplashScreenBinding
    private val viewModel: RoomActivityViewModel by viewModels()


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewSplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PowerPreference.init(this)

        viewModel

        GlobalScope.launch {
            delay(2000)
            move()

        }

    }

    private fun move(){
        val firebaseUser = Firebase.auth.currentUser

        when {
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
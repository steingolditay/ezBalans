package com.ezbalans.app.ezbalans

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.SignIn.Signup
import com.ezbalans.app.ezbalans.SignIn.Login
import com.ezbalans.app.ezbalans.databinding.ViewWelcomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ViewWelcomeBinding

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewWelcomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PowerPreference.init(this)


        val user = Firebase.auth.currentUser

        if (user != null){
            PowerPreference.getDefaultFile().setInt(Constants.main_activity_count, 0)
            PowerPreference.getDefaultFile().setInt(Constants.room_activity_count, 0)

            val intent = Intent(this, Mainframe::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.loginButton.setOnClickListener {
            val loginIntent = Intent(this, Login::class.java)
            startActivity(loginIntent)
        }
        binding.signupButton.setOnClickListener {
            val signupIntent = Intent(this, Signup::class.java)
            startActivity(signupIntent)
        }

    }
}
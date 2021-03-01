package com.ezbalans.app.ezbalans.views.signIn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.databinding.ViewWelcomeBinding
import com.preference.PowerPreference

class WelcomeActivity: AppCompatActivity() {
    private lateinit var binding: ViewWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewWelcomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PowerPreference.init(this)


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
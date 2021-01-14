package com.ezbalans.app.ezbalans.SignIn

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.Helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.Mainframe
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.ViewLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class  Login : AppCompatActivity(){
    private lateinit var binding: ViewLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.login.setOnClickListener{
            verifyDetails()
        }

        binding.forgotPassword.setOnClickListener {
            forgotPassword()
        }
    }
    private fun verifyDetails(){
        val email = binding.email.text.toString().trim();
        val password = binding.password.text.toString().trim()

        if (email.isEmpty()){
            binding.email.error = getString(R.string.email_required)
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.email.error = getString(R.string.invalid_email)

        }
        else if (password.isEmpty()){
            binding.password.error = getString(R.string.password_required)
        }
        else{
            userLogin(email, password)
        }

    }

    private fun userLogin(email: String, password: String){
        val auth = Firebase.auth;
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                val intent = Intent(this, Mainframe::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            else{
                Toast.makeText(this, resources.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun forgotPassword(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_forgot_password).create()
        val emailView = dialog.findViewById<EditText>(R.id.email)
        val reset = dialog.findViewById<Button>(R.id.reset_password)

        reset.setOnClickListener {
            val email = emailView.text.toString()
            Firebase.auth.sendPasswordResetEmail(email).addOnSuccessListener {
                Toast.makeText(this, String.format(getString(R.string.email_sent), email), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }



}
package com.ezbalans.app.ezbalans.presentation.signIn

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.presentation.HomeActivity
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.ViewLoginBinding
import com.ezbalans.app.ezbalans.utils.CustomDialogObject
import com.ezbalans.app.ezbalans.utils.LoadingDialog
import com.ezbalans.app.ezbalans.viewmodels.signinActivities.LoginActivityViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class  Login : AppCompatActivity(){

    private lateinit var binding: ViewLoginBinding
    private val viewModel: LoginActivityViewModel by viewModels()

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
        val email = binding.email.text.toString().trim()
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
        val loadingDialog = LoadingDialog(this, getString(R.string.logging_in)).create()
        loadingDialog.show()

        val auth = Firebase.auth
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful){

                viewModel.loginRepository()

                loadingDialog.dismiss()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            else {
                loadingDialog.dismiss()
                Toast.makeText(this, resources.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun forgotPassword(){
        val dialog = CustomDialogObject.create(this, R.layout.dialog_forgot_password)
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
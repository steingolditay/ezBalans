package com.ezbalans.app.ezbalans.views.signIn

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.helpers.Constants
import com.ezbalans.app.ezbalans.helpers.CheckPasswordStrength
import com.ezbalans.app.ezbalans.helpers.GetIdentityKey
import com.ezbalans.app.ezbalans.views.HomeActivity
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.ViewSignupBinding
import com.ezbalans.app.ezbalans.helpers.GetLoadingDialog
import com.ezbalans.app.ezbalans.viewmodels.roomActivities.CreateRoomActivityViewModel
import com.ezbalans.app.ezbalans.viewmodels.signinActivities.SignUpActivityViewModel
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*

class Signup : AppCompatActivity() {
    private lateinit var binding: ViewSignupBinding
    lateinit var existingKeyList: List<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewSignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.signup.setOnClickListener {
            verifyDetails()
        }

        val viewModel = SignUpActivityViewModel()
        viewModel.init()

        viewModel.getUserKeys().observe(this, {
            existingKeyList = it
        })

    }

    private fun verifyDetails() {
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val passwordVer = binding.passwordVer.text.toString().trim();
        val username = binding.username.text.toString().trim();
        val firstName = binding.firstName.text.toString().trim();
        val lastName = binding.lastName.text.toString().trim();

        val passStrength = CheckPasswordStrength().check(this, password)


        if (email.isEmpty()) {
            binding.email.error = getString(R.string.email_required)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.error = getString(R.string.invalid_email)
        } else if (passStrength != "") {
            binding.password.error = passStrength
        } else if (password != passwordVer) {
            binding.passwordVer.error = getString(R.string.password_doesnt_match)
        } else if (username.isEmpty()) {
            binding.username.error = getString(R.string.username_required)
        } else if (username.length < 4) {
            binding.username.error = getString(R.string.username_length)
        } else if (firstName.length < 2) {
            binding.firstName.error = getString(R.string.first_name_length)
        } else if (lastName.length < 2) {
            binding.lastName.error = getString(R.string.last_name_length)
        } else {
            registerUser(email, password, username)
        }

    }

    private fun registerUser(email: String, password: String, username: String) {
        val loadingDialog = GetLoadingDialog(this, getString(R.string.registering)).create()
        loadingDialog.show()

        val auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    createUser(email, username, loadingDialog)
                }
                task.exception is FirebaseAuthUserCollisionException -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, getText(R.string.user_collision), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, getText(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createUser(email: String, username: String, loadingDialog: Dialog) {
        val getIdentityKey = GetIdentityKey()
        val firebaseUser = Firebase.auth.currentUser!!
        val profileUpdates = UserProfileChangeRequest.Builder()
        profileUpdates.displayName = username
        profileUpdates.photoUri = Uri.parse(Constants.default_user_image)

        firebaseUser.updateProfile(profileUpdates.build()).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    firebaseUser.updateEmail(email)
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = Date()
                    val signUpDate = formatter.format(date)
                    val identityKey = getIdentityKey.create(existingKeyList)
                    val firstName = StringUtils.capitalize(binding.firstName.text.toString().toLowerCase(Locale.getDefault()).trim())
                    val lastName = StringUtils.capitalize(binding.lastName.text.toString().toLowerCase(Locale.getDefault()).trim())

                    val user = User(firebaseUser.uid, identityKey, email, username, firstName, lastName, Constants.default_user_image, signUpDate)

                    Firebase.database.reference.child(Constants.users).child(firebaseUser.uid).setValue(user).addOnSuccessListener{
                        Firebase.database.reference.child(Constants.budgets).child(firebaseUser.uid).child(firebaseUser.uid).setValue(0).addOnSuccessListener {
                            firebaseUser.sendEmailVerification()
                            loadingDialog.dismiss()
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                    }
                }
            }
        }
    }
}


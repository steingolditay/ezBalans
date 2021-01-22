package com.ezbalans.app.ezbalans

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.helpers.CheckPasswordStrength
import com.ezbalans.app.ezbalans.helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.databinding.ViewProfileBinding
import com.ezbalans.app.ezbalans.helpers.GetLoadingDialog
import com.ezbalans.app.ezbalans.helpers.GetPrefs
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.preference.PowerPreference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class Profile: AppCompatActivity() {
    private lateinit var binding: ViewProfileBinding

    private val databaseReference = Firebase.database.reference
    private val storageReference = Firebase.storage.reference
    private val firebaseUser = Firebase.auth.currentUser!!

    lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


//        Picasso.get().load(firebaseUser.photoUrl).into(binding.image)

        languageSelector()
        loadMyData()

        binding.image.setOnClickListener {
            loadImageCropper()
        }

        binding.identityKeyCopy.setOnClickListener{
            val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("label",user.identity_key)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(this, getString(R.string.identity_key_copied), Toast.LENGTH_SHORT).show()
        }

        binding.edit.setOnClickListener {
            openEditDialog()
        }


        binding.logout.setOnClickListener {
            openLogoutDialog()
        }

        if (firebaseUser.isEmailVerified){
            binding.emailVerification.visibility = View.GONE
        }
        else {
            binding.emailVerification.text = getString(R.string.email_not_verified)
            binding.emailVerification.setOnClickListener {
                openNotVerifiedDialog()
            }
        }

        binding.changeEmail.setOnClickListener {
            openAuthenticationDialog(Constants.email)
        }

        binding.changePassword.setOnClickListener {
            openAuthenticationDialog(Constants.password)

        }


    }

    private fun openAuthenticationDialog(source: String){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_authentication).create()
        val passwordField = dialog.findViewById<EditText>(R.id.password)
        val apply = dialog.findViewById<Button>(R.id.apply)

        apply.setOnClickListener {
            val credentials = EmailAuthProvider.getCredential(firebaseUser.email!!, passwordField.text.toString())
            firebaseUser.reauthenticate(credentials).addOnCompleteListener {
                if (it.isSuccessful){
                    dialog.dismiss()
                    when (source) {
                        Constants.email -> {
                            openChangeEmailDialog()
                        }
                        Constants.password -> {
                            openChangePasswordDialog()
                        }
                    }
                }
                else {
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun openChangeEmailDialog(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_edit_email).create()
        val emailField = dialog.findViewById<EditText>(R.id.email)
        val apply = dialog.findViewById<Button>(R.id.apply)


        emailField.setText(firebaseUser.email)

        apply.setOnClickListener {
            val email = emailField.text.toString()
            when {
                email.isEmpty() -> {
                    emailField.error = getString(R.string.email_required)
                }
                (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) -> {
                    emailField.error = getString(R.string.invalid_email)
                }
                else -> {
                    firebaseUser.updateEmail(email).addOnCompleteListener {
                        when {
                            it.isSuccessful -> {
                                Toast.makeText(this, getString(R.string.email_updated), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            it.exception is FirebaseAuthUserCollisionException -> {
                                Toast.makeText(this, getString(R.string.email_in_use), Toast.LENGTH_SHORT).show()

                            }
                            else -> {
                                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        }


                    }
                }

            }
        }

        dialog.show()
    }

    private fun openChangePasswordDialog(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_edit_password).create()
        val passField = dialog.findViewById<EditText>(R.id.password)
        val passVerFiled = dialog.findViewById<EditText>(R.id.passwordVer)
        val apply = dialog.findViewById<Button>(R.id.apply)



        apply.setOnClickListener {
            val password = passField.text.toString()
            val passwordVer = passVerFiled.text.toString()
            val passStrength = CheckPasswordStrength().check(this, password)

            when {
                password.isEmpty() -> {
                    passField.error = getString(R.string.password_required)
                }
                passStrength != "" -> {
                    passField.error = passStrength
                }
                password != passwordVer -> {
                    passVerFiled.error = getString(R.string.password_doesnt_match)
                }

                else -> {
                    firebaseUser.updatePassword(password).addOnCompleteListener {
                        when {
                            it.isSuccessful -> {
                                Toast.makeText(this, getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            else -> {
                                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        }


                    }
                }

            }
        }

        dialog.show()
    }

    private fun openNotVerifiedDialog(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_send_email_verification).create()
        val sendEmail = dialog.findViewById<Button>(R.id.send_email)

        sendEmail.setOnClickListener {
            firebaseUser.sendEmailVerification().addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this, getString(R.string.verification_email_sent), Toast.LENGTH_SHORT).show()

                }
                else {
                    Log.d("TAG", "openNotVerifiedDialog: ${it.result}")
                }
                dialog.dismiss()

            }
        }
        dialog.show()

    }

    private fun languageSelector(){
        val lang = PowerPreference.getDefaultFile().getString(Constants.language)
        if (lang == Constants.language_hebrew){
            binding.hebrew.isChecked = true
        }
        else{
            binding.english.isChecked = true
        }
        binding.radioGroup.setOnCheckedChangeListener() { _, checkedId ->
            when (checkedId){
                binding.english.id -> {
                    binding.notifyChange.visibility = if (lang != Constants.language_english) View.VISIBLE else View.GONE
                    PowerPreference.getDefaultFile().putString(Constants.language, Constants.language_english)

                }
                binding.hebrew.id -> {

                    binding.notifyChange.visibility = if (lang != Constants.language_hebrew) View.VISIBLE else View.GONE
                    PowerPreference.getDefaultFile().putString(Constants.language, Constants.language_hebrew)
                }
            }

        }
    }

    private fun loadMyData(){
        val usersPref = GetPrefs().getAllUsers()
        val myUser= usersPref[firebaseUser.uid]!!
        Picasso.get().load(myUser.image).into(binding.image)

        user = usersPref[firebaseUser.uid]!!

        binding.name.text = "${user.first_name} ${user.last_name}"
        binding.username.text = user.username
        binding.signupDate.text = user.signup_date
        binding.email.text = user.email
        binding.identityKey.text = user.identity_key
    }

    private fun loadImageCropper(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this)
    }

    private fun setNewImage(uri: Uri){
        val dialog = GetLoadingDialog(this, getString(R.string.uploading_image)).create()
        dialog.show()
        storageReference.child(Constants.users).child(firebaseUser.uid).child(Constants.image).putFile(uri).addOnSuccessListener {
            storageReference.child(Constants.users).child(firebaseUser.uid).child(Constants.image).downloadUrl.addOnSuccessListener {
                val imageUri = it.toString()
                databaseReference.child(Constants.users).child(firebaseUser.uid).child(Constants.image).setValue(imageUri).addOnSuccessListener {
                    val profileUpdates = userProfileChangeRequest { photoUri = uri }
                    firebaseUser.updateProfile(profileUpdates).addOnSuccessListener {
                        Picasso.get().load(firebaseUser.photoUrl).into(binding.image)
                        dialog.dismiss()


                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == RESULT_OK){
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                val result = CropImage.getActivityResult(data)
                setNewImage(result.uri)
            }
        }
    }

    private fun openEditDialog(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_edit_profile).create()

        val firstName = dialog.findViewById<EditText>(R.id.first_name)
        val lastName = dialog.findViewById<EditText>(R.id.last_name)
        val username = dialog.findViewById<EditText>(R.id.username)
        val apply = dialog.findViewById<Button>(R.id.apply)
        val firstNameBox = dialog.findViewById<TextInputLayout>(R.id.first_name_box)
        val lastNameBox = dialog.findViewById<TextInputLayout>(R.id.last_name_box)
        val usernameBox = dialog.findViewById<TextInputLayout>(R.id.username_box)

        firstNameBox.hint = user.first_name
        lastNameBox.hint = user.last_name
        usernameBox.hint = user.username

        firstName.setOnFocusChangeListener { _ , hasFocus ->
            if (hasFocus){
                firstNameBox.hint = getString(R.string.first_name_hint)
            }
            else  if (firstName.text.toString().isEmpty()) {
                firstNameBox.hint = user.first_name

            }
        }

        lastName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                lastNameBox.hint = getString(R.string.last_name_hint)
            }
            else  if (lastName.text.toString().isEmpty()) {
                lastNameBox.hint = user.last_name

            }
        }

        username.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                usernameBox.hint = getString(R.string.username_hint)
            }
            else  if (username.text.toString().isEmpty()) {
                usernameBox.hint = user.username

            }
        }

        apply.setOnClickListener {
            val mapValues = HashMap<String, Any>()

            val newFirstName = firstName.text.toString()
            val newLastName = lastName.text.toString()
            val newUsername = username.text.toString()

            if (newFirstName.isNotEmpty() && newFirstName != user.first_name){
                mapValues[Constants.first_name] = newFirstName
            }
            if (newLastName.isNotEmpty() && newLastName != user.last_name){
                mapValues[Constants.last_name] = newLastName
            }
            if (newUsername.isNotEmpty() && newUsername != user.username){
                mapValues[Constants.username] = newUsername

                val profileUpdates = userProfileChangeRequest { displayName = newUsername }
                firebaseUser.updateProfile(profileUpdates)
            }

            if (mapValues.isNotEmpty()){
                dialog.dismiss()
                val loadingDialog = GetLoadingDialog(this, getString(R.string.Updating_info)).create()
                loadingDialog.show()
                databaseReference.child(Constants.users).child(firebaseUser.uid).updateChildren(mapValues).addOnSuccessListener {
                    loadMyData()
                    loadingDialog.dismiss()
                }
            }
            else {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun openLogoutDialog(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_logout).create()
        val logout = dialog.findViewById<Button>(R.id.logout)

        logout.setOnClickListener {
            dialog.dismiss()
            val loadingDialog = GetLoadingDialog(this, getString(R.string.logging_out)).create()
            loadingDialog.show()

            Firebase.auth.signOut()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            loadingDialog.dismiss()
            startActivity(intent)
            finish()
        }
        dialog.show()
    }

}
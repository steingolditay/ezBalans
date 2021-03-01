package com.ezbalans.app.ezbalans.views.rooms.roomActivities

import android.Manifest
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.RowUserBinding
import com.ezbalans.app.ezbalans.databinding.ViewRoomInfoBinding
import com.ezbalans.app.ezbalans.helpers.*
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.viewmodels.roomActivities.RoomActivityViewModel
import com.ezbalans.app.ezbalans.viewmodels.roomActivities.RoomInfoActivityViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.preference.PowerPreference
import com.skydoves.powerspinner.PowerSpinnerView
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*
import kotlin.collections.HashMap

class RoomInfo : AppCompatActivity() {
    private lateinit var binding: ViewRoomInfoBinding


    val firebaseUser = Firebase.auth.currentUser!!
    val databaseReference = Firebase.database.reference
    private val storageReference = Firebase.storage.reference
    val admins = arrayListOf<String>()
    val residents = arrayListOf<String>()
    val users = HashMap<String, User>()
    var room = Room()
    var admin = false
    private lateinit var roomUID: String

    private val roomTypes = Constants.room_types
    private val roomCategories = HashMap<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewRoomInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val bundle = intent.extras
        if (bundle != null) {
            roomUID = bundle.getString(Constants.room_uid)!!
            val admin = bundle.getBoolean(Constants.admin)

            this.admin = admin
            getRoom(roomUID)
            when {
                admin -> {
                    initAdmin()
                }
                else -> {
                    initResident()
                }
            }
        }
        binding.image.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadImageCropper()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.IMAGE_CROP_REQUEST_CODE)
            }
        }

        binding.motdCard.setOnClickListener {
            openMOTDDialog()
        }

        val viewModel = RoomInfoActivityViewModel()
        viewModel.init()

        viewModel.getAllUsers().observe(this, {

        })

        viewModel.getAllRooms().observe(this, {
            for (roomObject in it){
                if (roomObject.uid == roomUID){
                    room = roomObject

                    loadRoomDetails()
                }
            }
        })

    }

    private fun getRoom(roomUid: String) {
        val roomsPref = GetPrefs().getAllRooms()
        room = roomsPref[roomUid]!!

        loadResidents()

    }

    private fun loadResidents() {
        binding.adminsContainer.removeAllViews()
        binding.residentsContainer.removeAllViews()


        for (residentUser in residents){
            val user = users[residentUser]!!
            val rowView = LayoutInflater.from(this).inflate(R.layout.row_user, binding.residentsContainer, false)
            val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(10, 10, 10, 10)
            if (admin) {
                val remove = rowView.findViewById<ImageView>(R.id.remove)
                val promote = rowView.findViewById<ImageView>(R.id.promote)
                remove.visibility = View.VISIBLE
                promote.visibility = View.VISIBLE

                remove.setOnClickListener {
                    removeResidentDialog(user, rowView)

                }

                promote.setOnClickListener {
                    promoteResidentDialog(user, rowView)
                }
            }
            val image = rowView.findViewById<ImageView>(R.id.image)
            val username = rowView.findViewById<TextView>(R.id.username)
            val name = rowView.findViewById<TextView>(R.id.name)
            val identityKey = rowView.findViewById<TextView>(R.id.identity_key)

            Picasso.get().load(user.image).into(image)
            username.text = user.username
            name.text = String.format("%s %s", user.first_name, user.last_name)
            identityKey.text = user.identity_key

            rowView.setOnLongClickListener {
                val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("label", user.identity_key)
                clipboard.setPrimaryClip(clipData)
                Toast.makeText(this, getString(R.string.identity_key_copied), Toast.LENGTH_SHORT).show()
                true

            }
            binding.residentsContainer.addView(rowView, layoutParams)
        }

        for (adminUser in admins){
            val user = users[adminUser]!!

            val rowView = LayoutInflater.from(this).inflate(R.layout.row_user, binding.adminsContainer, false)
            val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(10, 10, 10, 10)

            if (admin) {
                val remove = rowView.findViewById<ImageView>(R.id.remove)
                remove.setOnClickListener {
                    removeAdminDialog(user, rowView)
                }
            }
            val image = rowView.findViewById<ImageView>(R.id.image)
            val username = rowView.findViewById<TextView>(R.id.username)
            val name = rowView.findViewById<TextView>(R.id.name)
            val identityKey = rowView.findViewById<TextView>(R.id.identity_key)

            Picasso.get().load(user.image).into(image)
            username.text = user.username
            name.text = String.format("%s %s", user.first_name, user.last_name)
            identityKey.text = user.identity_key

            rowView.setOnLongClickListener {
                val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("label", user.identity_key)
                clipboard.setPrimaryClip(clipData)
                Toast.makeText(this, getString(R.string.identity_key_copied), Toast.LENGTH_SHORT).show()
                true

            }
            binding.adminsContainer.addView(rowView, layoutParams)
        }
    }

    private fun loadRoomDetails() {
        Picasso.get().load(room.image).into(binding.image)

        for (admin in room.admins.keys){
            if (room.admins[admin] == true){
                admins.add(admin)
            }
        }
        for (resident in room.residents.keys){
            if (!room.admins.containsKey(resident) && room.residents[resident]== Constants.added){
                residents.add(resident)
            }
        }

        binding.name.text = room.name
        binding.creationDate.text = String.format(getString(R.string.created_on), room.creation_date)
        binding.budget.text = room.monthly_budget
        binding.description.text = room.description
        binding.motd.text = room.motd

        val lang = PowerPreference.getDefaultFile().getString(Constants.language)
        if (lang == Constants.language_hebrew){
            binding.roomCurrency.text = TranslateToHebrew().roomCurrency(room.currency)
            binding.roomType.text = TranslateToHebrew().roomType(room.type)
        }
        else {
            binding.roomCurrency.text = room.currency
            binding.roomType.text = room.type
        }

    }

    private fun loadImageCropper() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this)
    }

    private fun uploadImage(imageUri: Uri) {
        val loadingDialog = GetLoadingDialog(this, getString(R.string.uploading_image)).create()
        loadingDialog.show()

        storageReference.child(Constants.room).child(room.uid).child(Constants.image).putFile(imageUri).addOnSuccessListener {
            storageReference.child(Constants.room).child(room.uid).child(Constants.image).downloadUrl.addOnSuccessListener { result ->
                val stringUri = result.toString()
                databaseReference.child(Constants.rooms).child(room.uid).child(Constants.image).setValue(stringUri).addOnSuccessListener {
                    loadingDialog.dismiss()
                }
            }

        }
    }

    private fun removeResidentDialog(user: User, view: View) {
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_remove_resident).create()
        val remove = dialog.findViewById<Button>(R.id.remove)
        remove.setOnClickListener {
            removeResident(user, view)
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun removeAdminDialog(user: User, view: View) {
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_remove_admin).create()
        val removeAdmin = dialog.findViewById<Button>(R.id.remove_admin)
        val remove = dialog.findViewById<Button>(R.id.remove)


        removeAdmin.setOnClickListener {
            demoteAdmin(user, view)
            dialog.dismiss()

        }

        remove.setOnClickListener {
            demoteAdmin(user, view)
            removeResident(user, view)
            dialog.dismiss()

        }

        dialog.show()

    }

    private fun promoteResidentDialog(user: User, view: View) {
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_promote_resident).create()
        val promote = dialog.findViewById<Button>(R.id.promote)

        promote.setOnClickListener {
            promoteResident(user, view)
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun removeResident(user: User, view: View) {
        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(user.uid).setValue(
                Constants.removed).addOnSuccessListener {
            CreateNotification().create(room, Constants.notify_user_removed, firebaseUser.uid, user.uid, "")

            binding.residentsContainer.removeView(view)
        }

    }

    private fun demoteAdmin(user: User, view: View) {
        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.admins).child(user.uid).setValue(false).addOnSuccessListener {
            CreateNotification().create(room, Constants.notify_admin_demoted, firebaseUser.uid, user.uid, "")

            binding.adminsContainer.removeView(view)
            binding.residentsContainer.addView(view)

        }
    }

    private fun promoteResident(user: User, view: View) {
        val rowViewBinding = RowUserBinding.inflate(layoutInflater)
        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.admins).child(user.uid).setValue(true).addOnSuccessListener {
            CreateNotification().create(room, Constants.notify_admin_promoted, firebaseUser.uid, user.uid, "")

            binding.residentsContainer.removeView(view)
            binding.adminsContainer.addView(view)
            rowViewBinding.promote.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.IMAGE_CROP_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImageCropper()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            val imageUri = result.uri
            Picasso.get().load(imageUri).into(binding.image)
            uploadImage(imageUri)
        }
    }

    private fun initResident() {
        binding.edit.visibility = View.GONE
        binding.edit.setOnClickListener(null)
        binding.closeRoom.visibility = View.GONE

    }

    private fun initAdmin() {
        binding.edit.visibility = View.VISIBLE
        binding.closeRoom.visibility = View.VISIBLE

        binding.edit.setOnClickListener {
            openEditDialog()
        }

        binding.closeRoom.setOnClickListener {
            closeRoomDialog()
        }


    }

    private fun openEditDialog() {
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_edit_room_info).create()

        val name = dialog.findViewById<EditText>(R.id.name)
        val desc = dialog.findViewById<EditText>(R.id.desc)
        val budget = dialog.findViewById<EditText>(R.id.budget)
        val nameBox = dialog.findViewById<TextInputLayout>(R.id.name_box)
        val descBox = dialog.findViewById<TextInputLayout>(R.id.desc_box)
        val budgetBox = dialog.findViewById<TextInputLayout>(R.id.budget_box)
        val typeSpinner = dialog.findViewById<PowerSpinnerView>(R.id.room_type_spinner)
        val currencySpinner = dialog.findViewById<PowerSpinnerView>(R.id.room_currency_spinner)

        val apply = dialog.findViewById<Button>(R.id.apply)

        val currenciesList = resources.getStringArray(R.array.room_currencies).toList()
        val typesList = resources.getStringArray(R.array.room_types).toList()

        currencySpinner.setItems(currenciesList)
        typeSpinner.setItems(typesList)

        var roomType = ""
        var roomCurrency = ""
        var categories = arrayListOf<String>()

        nameBox.hint = room.name
        descBox.hint = room.description
        budgetBox.hint = room.monthly_budget

        name.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                nameBox.hint = getString(R.string.name)
                if (name.text.isEmpty()){
                    name.setText(room.name)
                }
            } else if (name.text.toString().isEmpty()) {
                nameBox.hint = room.name

            }
        }
        desc.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                descBox.hint = getString(R.string.description)
                if (desc.text.isEmpty()){
                    desc.setText(room.description)
                }
            } else if (desc.text.toString().isEmpty()) {
                descBox.hint = room.description

            }
        }
        budget.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                budgetBox.hint = getString(R.string.budget)
                if (budget.text.isEmpty()){
                    budget.setText(room.monthly_budget)
                }
            } else if (budget.text.toString().isEmpty()) {
                budgetBox.hint = room.monthly_budget

            }
        }

        for (i in 0 until roomTypes.size) {
            if (room.type == roomTypes[i]) {
                typeSpinner.selectItemByIndex(i)
            }
        }

        for (i in 0 until Constants.room_currencies.size){
            if (room.currency == Constants.room_currencies[i]){
                currencySpinner.selectItemByIndex(i)
            }
        }

        currencySpinner.setOnSpinnerItemSelectedListener<String>{ position, item ->
            roomCurrency = Constants.room_currencies[position]
        }

        typeSpinner.setOnSpinnerItemSelectedListener<String> { position, item ->
            roomType = Constants.room_types[position]
            when (item) {
                typesList[0] -> {
                    categories = Constants.room_category_family
                }
                typesList[1] -> {
                    categories = Constants.room_category_roommates
                }
                typesList[2] -> {
                    categories = Constants.room_category_couple
                }
                typesList[4] -> {
                    categories = Constants.room_category_vacation
                }

            }

            for (category in Constants.room_category_family) {
                when (categories.contains(category)) {
                    true -> {
                        roomCategories[category] = true
                    }
                    false -> {
                        roomCategories[category] = false
                    }
                }
            }
        }

        apply.setOnClickListener {
            val mapValues = HashMap<String, Any>()

            val newName = name.text.toString()
            val newDesc = desc.text.toString()
            val newBudget = budget.text.toString()

            if (newName.isNotEmpty() && newName != room.name) {
                mapValues[Constants.name] = newName
            }
            if (newDesc.isNotEmpty() && newDesc != room.description) {
                mapValues[Constants.description] = newDesc
            }
            if (newBudget.isNotEmpty() && newBudget != room.monthly_budget) {
                mapValues[Constants.monthly_budget] = newBudget
            }

            if (roomType.isNotEmpty() && roomType != room.type){
                mapValues[Constants.type] = roomType
                mapValues[Constants.categories] = roomCategories
            }
            if (roomCurrency.isNotEmpty() && roomCurrency != room.currency){
                mapValues[Constants.currency] = roomCurrency
            }

            if (mapValues.isNotEmpty()) {
                databaseReference.child(Constants.rooms).child(room.uid).updateChildren(mapValues).addOnSuccessListener {
                    CreateNotification().create(room, Constants.notify_room_info_changed, firebaseUser.uid, "", "")
                    getRoom(room.uid)
                    dialog.dismiss()
                }
            } else {
                dialog.dismiss()
            }
        }


        dialog.show()
    }

    private fun closeRoomDialog() {
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_close_room).create()

        val updates = hashMapOf<String, Any>()
        updates[Constants.status] = Constants.room_inactive
        updates[Constants.room_closing_date] = GetCurrentDate().dateFromTimestamp(Date().time)

        binding.closeRoom.setOnClickListener {
            databaseReference.child(Constants.rooms).child(room.uid).updateChildren(updates).addOnSuccessListener {
                CreateNotification().create(room, Constants.notify_room_closed, firebaseUser.uid, "", "")
                finish()
                Toast.makeText(this, getString(R.string.room_closed_toast), Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun openMOTDDialog(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_edit_motd).create()
        val motd = dialog.findViewById<EditText>(R.id.motd)
        val apply = dialog.findViewById<Button>(R.id.apply)

        if (room.motd.isNotEmpty()){
            motd.setText(room.motd)
        }

        apply.setOnClickListener {
            val message = motd.text.toString()
            if (message != room.motd){
                databaseReference.child(Constants.rooms).child(room.uid).child(Constants.motd).setValue(message).addOnSuccessListener {
                    CreateNotification().create(room, Constants.notify_motd_changed, firebaseUser.uid, "", message)
                    getRoom(room.uid)
                    dialog.dismiss()

                }
            }
            else {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

}
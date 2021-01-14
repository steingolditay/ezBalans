package com.ezbalans.app.ezbalans.Rooms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezbalans.app.ezbalans.Helpers.GetCurrentDate
import com.ezbalans.app.ezbalans.Helpers.GetIdentityKey
import com.ezbalans.app.ezbalans.Models.User
import com.ezbalans.app.ezbalans.Models.Room
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.RowUserBinding
import com.ezbalans.app.ezbalans.databinding.ViewCreateRoomBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.HashMap

class CreateRoom: AppCompatActivity(){
    private lateinit var binding: ViewCreateRoomBinding


    private val firebaseUser = Firebase.auth.currentUser
    private val databaseReference = Firebase.database.reference
    var userList = arrayListOf<User>()
    private var addedUsers = arrayListOf<String>()
    private val identityKeys = GetIdentityKey()
    private val getCurrentDate = GetCurrentDate()

    var roomType = ""
    var roomCurrency = ""
    private val roomCategories = HashMap<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewCreateRoomBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        getUsers()

//        val roomCurrencies = resources.getStringArray(R.array.room_currencies).toList()
        binding.roomCurrencySpinner.setItems(resources.getStringArray(R.array.room_currencies).toList())
        binding.roomCurrencySpinner.setOnSpinnerItemSelectedListener<String>(){position, _ ->
            roomCurrency = Constants.room_currencies[position]
        }

        val roomTypes = resources.getStringArray(R.array.room_types).toList()
        binding.roomTypeSpinner.setItems(resources.getStringArray(R.array.room_types).toList())
        binding.roomTypeSpinner.setOnSpinnerItemSelectedListener<String> { position, item ->
            roomType = Constants.room_types[position]
            var categories = arrayListOf<String>()
            when (item){
                roomTypes[0] -> {
                    categories = Constants.room_category_family
                }
                roomTypes[1] -> {
                    categories = Constants.room_category_roommates
                }
                roomTypes[2] -> {
                    categories = Constants.room_category_couple

                }
                roomTypes[3] -> {
                    categories = Constants.room_category_vacation
                }
            }

            for (category in Constants.room_category_family){
                when (categories.contains(category)){
                    true -> {
                        roomCategories[category] = true
                    }
                    false -> {
                        roomCategories[category] = false
                    }
                }
            }
        }


        binding.add.setOnClickListener {
            findUser()
            binding.identity.setText("")

        }

        binding.create.setOnClickListener {
            validateDetails()
        }
    }


    private fun getUsers(){
            databaseReference.child(Constants.users).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (users in snapshot.children) {
                        val user = users.getValue(User::class.java)!!
                        if (user.uid != firebaseUser?.uid) {
                            userList.add(user)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun findUser(){
        var found = false
        val userIdentityKey = binding.identity.text.toString().toUpperCase(Locale.getDefault()).trim();
        for (user in userList){
            if (user.identity_key == userIdentityKey){
                found = true
                if (!addedUsers.contains(user.uid)){
                    addedUsers.add(user.uid)

                    addUser(user)
                }
                else{
                    Toast.makeText(this, getString(R.string.user_already_added), Toast.LENGTH_SHORT).show()

                }
            }
        }
        if (!found){
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUser(user: User){
        val rowBinding: RowUserBinding = RowUserBinding.inflate(layoutInflater)
        val rowView = LayoutInflater.from(this).inflate(R.layout.row_user, binding.list, false)
        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(10,10,10,10)


        if (user.image.isNotEmpty()){
            Picasso.get().load(user.image).into(rowBinding.image)
        }
        else{
            Picasso.get().load(R.drawable.default_account).into(rowBinding.image)

        }
        rowBinding.username.text = user.username;
        rowBinding.name.text = String.format("%s %s", user.first_name, user.last_name)
        rowBinding.identityKey.text = user.identity_key
        rowBinding.remove.visibility = View.VISIBLE

        rowBinding.remove.setOnClickListener{
            binding.list.removeView(rowView)
            addedUsers.remove(user.uid)

        }

        binding.list.addView(rowView, layoutParams)

    }

    private fun validateDetails(){
        val name  = findViewById<TextView>(R.id.name)
        val roomName = name.text.toString().trim()
        val roomBudget = binding.budget.text.toString().trim()

        when {
            roomName.isEmpty() -> {
                name.error = getString(R.string.room_name_required)
            }
            roomBudget.isEmpty() -> {
                binding.budget.error = getString(R.string.room_budget_required)
            }
            roomType == "" -> {
                binding.roomTypeSpinner.error = getString(R.string.room_type_not_selected)
            }

            roomCurrency == "" -> {
                binding.roomCurrencySpinner.error = getString(R.string.currency_not_selected)
            }
            else -> {
                createRoom()
            }
        }

    }

    private fun createRoom(){
        val name  = findViewById<TextView>(R.id.name)

        val roomUid = UUID.randomUUID().toString()
        val shoppingList = UUID.randomUUID().toString()
        val identityKey = identityKeys.create()
        val roomName = name.text.toString().trim()
        val roomDescription = binding.description.text.toString().trim()
        val creationDate = getCurrentDate.formatted()
        val roomBudget = binding.budget.text.toString().trim()

        val admins = HashMap<String, Boolean>()
        admins[firebaseUser!!.uid] = true

        val residents = HashMap<String, String>()
        residents[firebaseUser!!.uid] = Constants.added
        for (uid in addedUsers){
            residents[uid] = Constants.added
        }

        val room = Room(roomUid,
                identityKey,
                roomName,
                roomDescription,
                Constants.default_room_image,
                creationDate,
                "",
                roomBudget,
                admins,
                residents,
                shoppingList,
                roomType,
                roomCurrency,
                roomCategories,
                Constants.room_active,
                getString(R.string.default_motd))

        databaseReference.child(Constants.rooms).child(roomUid).setValue(room).addOnCompleteListener() { result ->
            if (result.isSuccessful){
                databaseReference.child(Constants.shopping_lists).child(shoppingList).child(getString(R.string.example_item)).setValue(true)
                finish()
            }
        }

    }
}
package com.ezbalans.app.ezbalans.presentation.rooms.roomActivities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.ViewCreateRoomBinding
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.utils.*
import com.ezbalans.app.ezbalans.viewmodels.roomActivities.CreateRoomActivityViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.HashMap

@AndroidEntryPoint
class CreateRoom: AppCompatActivity(){
    private lateinit var binding: ViewCreateRoomBinding


    private val firebaseUser = Firebase.auth.currentUser
    private val databaseReference = Firebase.database.reference
    private var addedUsers = arrayListOf<String>()
    private val identityKeys = IdentityKeys()
    private val getCurrentDate = DateAndTimeUtils()
    private lateinit var existingKeyList: List<String>
    private lateinit var userList: List<User>
    private var uiLoaded = false

    private val viewModel: CreateRoomActivityViewModel by viewModels()

    private var roomType = ""
    private var roomCurrency = ""
    private val roomCategories = HashMap<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewCreateRoomBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        viewModel.getAllUsers().observe(this, {
            userList = it.values.toList()
            loadUI()

        })

        viewModel.getAllRoomKeys().observe(this, {
            existingKeyList = it
            loadUI()
        })

        binding.add.setOnClickListener {
            findUser()
            binding.identity.setText("")

        }

        binding.create.setOnClickListener {
            validateDetails()
        }
    }

    private fun loadUI(){
        if (this::userList.isInitialized && this::existingKeyList.isInitialized && !uiLoaded){
            uiLoaded = true

            binding.roomCurrencySpinner.setItems(resources.getStringArray(R.array.room_currencies).toList())
            binding.roomCurrencySpinner.setOnSpinnerItemSelectedListener<String>{position, _ ->
                roomCurrency = Constants.room_currencies[position]
            }

            val roomTypes = resources.getStringArray(R.array.room_types).toList()
            binding.roomTypeSpinner.setItems(resources.getStringArray(R.array.room_types).toList())
            binding.roomTypeSpinner.setOnSpinnerItemSelectedListener<String>{ position, item ->
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
        }


    }


    private fun findUser(){
        var found = false
        val userIdentityKey = binding.identity.text.toString().toUpperCase(Locale.getDefault()).trim()
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
        val rowView = LayoutInflater.from(this).inflate(R.layout.row_user, binding.list, false)
        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(10,10,10,10)

        val image = rowView.findViewById<CircleImageView>(R.id.image)
        val username = rowView.findViewById<TextView>(R.id.username)
        val name = rowView.findViewById<TextView>(R.id.name)
        val identityKey = rowView.findViewById<TextView>(R.id.identity_key)
        val remove = rowView.findViewById<ImageView>(R.id.remove)


        Picasso.get().load(user.image).into(image)

        username.text = user.username
        name.text = String.format("%s %s", user.first_name, user.last_name)
        identityKey.text = user.identity_key
        remove.visibility = View.VISIBLE

        remove.setOnClickListener{
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
        val dialog = LoadingDialog(this, "Creating Room").create()
        dialog.show()

        val name  = findViewById<TextView>(R.id.name)

        val roomUid = UUID.randomUUID().toString()
        val shoppingList = UUID.randomUUID().toString()
        val identityKey = identityKeys.create(existingKeyList)
        val roomName = name.text.toString().trim()
        val roomDescription = binding.description.text.toString().trim()
        val creationDate = getCurrentDate.formattedCurrentDateString()
        val roomBudget = binding.budget.text.toString().trim()

        val admins = HashMap<String, Boolean>()
        admins[firebaseUser!!.uid] = true

        val residents = HashMap<String, String>()
        residents[firebaseUser.uid] = Constants.added
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

        // update database
        databaseReference.child(Constants.rooms).child(roomUid).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    dialog.dismiss()
                    finish()

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        viewModel.createRoom(this, room)




    }
}
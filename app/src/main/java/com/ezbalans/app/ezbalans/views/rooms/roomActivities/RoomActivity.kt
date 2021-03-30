package com.ezbalans.app.ezbalans.views.rooms.roomActivities

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.ViewRoomBinding
import com.ezbalans.app.ezbalans.helpers.Constants
import com.ezbalans.app.ezbalans.helpers.CreateNotification
import com.ezbalans.app.ezbalans.helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import com.ezbalans.app.ezbalans.viewmodels.roomActivities.RoomActivityViewModel
import com.ezbalans.app.ezbalans.views.rooms.roomFragments.FragmentDetails
import com.ezbalans.app.ezbalans.views.rooms.roomFragments.FragmentHistory
import com.ezbalans.app.ezbalans.views.rooms.roomFragments.FragmentStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class RoomActivity: AppCompatActivity(){
    private lateinit var binding: ViewRoomBinding

    companion object{
        var currentFragment = ""
    }

    private val databaseReference = Firebase.database.reference
    private val firebaseUser = Firebase.auth.currentUser!!
    var admin = false
    var room: Room = Room()
    private var roomUID: String? = ""
    var userList = arrayListOf<User>()

    private val fragmentStatus = FragmentStatus()
    private val fragmentDetails = FragmentDetails()
    private val fragmentHistory = FragmentHistory()
    private val fragmentBundle = Bundle()

    var fragmentSelector = ""
    @Inject lateinit var repository: DatabaseRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewRoomBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle = intent.extras
        if (bundle != null){
            if (bundle.containsKey(Constants.room_uid) && bundle.containsKey(Constants.fragmentSelector)){
                roomUID = bundle.getString(Constants.room_uid)
                fragmentSelector = bundle.getString(Constants.fragmentSelector).toString()
                fragmentBundle.putString(Constants.room_uid, roomUID)

                fragmentStatus.arguments = fragmentBundle
                fragmentDetails.arguments = fragmentBundle
                fragmentHistory.arguments = fragmentBundle

            }

        }

        val viewModel = RoomActivityViewModel(repository)

        viewModel.getAllUsers().observe(this, {
            for (user in it.values){
                userList.add(user)
            }
        })

        viewModel.getAllRooms().observe(this, {
            for (roomObject in it){
                if (roomObject.uid == roomUID){
                    room = roomObject
                    loadRoom()
                    if (room.admins[firebaseUser.uid] == true){
                        admin = true
                    }
                }
            }
        })


        when (fragmentSelector) {
            Constants.status_tag -> {
                setFragment(fragmentStatus, Constants.status_tag)
                currentFragment = Constants.status_tag
                binding.bottomBar.itemActiveIndex = 0
            }
            Constants.details_tag -> {
                setFragment(fragmentDetails, Constants.details_tag)
                currentFragment = Constants.details_tag
                binding.bottomBar.itemActiveIndex = 1

            }
            Constants.history_tag -> {
                setFragment(fragmentHistory, Constants.history_tag)
                currentFragment = Constants.history_tag
                binding.bottomBar.itemActiveIndex = 2

            }
        }

        binding.options.setOnClickListener{
            showOptions(it)
        }


        binding.bottomBar.onItemSelected = {
            when (it){
                0 -> {
                    setFragment(fragmentStatus, Constants.status_tag)
                }
                1 -> {
                    setFragment(fragmentDetails, Constants.details_tag)
                }
                2 -> {
                    setFragment(fragmentHistory, Constants.history_tag)
                }
            }
        }

    }


    private fun loadRoom(){
        Picasso.get().load(room.image).into(binding.image)
        binding.name.text = room.name
    }

    private fun setFragment(fragment: Fragment, tag: String){
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.mainframe, fragment, tag)
            .commit()
        currentFragment = tag
    }

    private fun showOptions(view: View){
        val optionsPopup = PopupMenu(this, view)
        val inflater = optionsPopup.menuInflater
        inflater.inflate(R.menu.menu_room_options, optionsPopup.menu)

        optionsPopup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    goToRoomSettings()
                }

                R.id.share -> {
                    shareRoom()
                }

                R.id.add -> {
                    addResident()
                }
                R.id.leave -> {
                    leaveRoom()
                }
            }
            true
        })
        optionsPopup.show()
    }

    private fun goToRoomSettings(){
        val intent = Intent(this, RoomInfo::class.java)
        intent.putExtra(Constants.room_uid, roomUID)
        intent.putExtra(Constants.admin, admin)
        startActivity(intent)
    }

    private fun leaveRoom(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_leave_room).create()

        val leaveRoom  = dialog.findViewById<Button>(R.id.leave_room)
        leaveRoom.setOnClickListener {
            if (admin){
                when {
                    room.admins.size > 1 -> {
                        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.admins).child(firebaseUser.uid).setValue(false)
                        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(firebaseUser.uid).setValue(
                                Constants.quit)
                        CreateNotification().create(room, Constants.notify_user_quit, firebaseUser.uid, "", "")
                    }
                    // choose a random resident to become admin
                    room.residents.size > 1 -> {
                        val residents = room.admins.keys.toTypedArray()
                        val random = (0..residents.size).random()
                        val resident = room.residents[residents[random]]!!
                        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.admins).child(firebaseUser.uid).setValue(false)
                        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(firebaseUser.uid).setValue(
                                Constants.quit)
                        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.admins).child(resident).setValue(true)
                        CreateNotification().create(room, Constants.notify_user_quit, firebaseUser.uid, "", "")

                    }
                    else -> {
                        closeRoomDialog()

                    }
                }
            }
            else {
                databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(firebaseUser.uid).setValue(
                        Constants.quit)

            }
            dialog.dismiss()
            finish()
            Toast.makeText(this, getString(R.string.you_left_the_room), Toast.LENGTH_SHORT).show()
        }

        dialog.show()

    }

    private fun addResident(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_add_resident).create()
        val identityText = dialog.findViewById<EditText>(R.id.identity)
        val addResident = dialog.findViewById<Button>(R.id.add_resident)

        addResident.setOnClickListener {
            val identityKey =identityText.text.toString()
            var found = false
            for (user in userList){
                if (user.identity_key == identityKey){
                    found = true
                    databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(user.uid).setValue(
                            Constants.added).addOnSuccessListener {
                        CreateNotification().create(room, Constants.notify_user_joined, firebaseUser.uid, user.uid, "")
                        dialog.dismiss()
                        Toast.makeText(this, getString(R.string.resident_added), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (!found){
                Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
            }

        }

        dialog.show()

    }

    private fun closeRoomDialog(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_close_room).create()
        val body = dialog.findViewById<TextView>(R.id.body)
        val closeRoom = dialog.findViewById<Button>(R.id.close_room)

        body.text = getString(R.string.no_more_residents)

        closeRoom.setOnClickListener {
            databaseReference.child(Constants.rooms).child(room.uid).child(Constants.status).setValue(
                    Constants.room_inactive).addOnSuccessListener {
                CreateNotification().create(room, Constants.notify_room_closed, firebaseUser.uid, "", "")
                finish()
                Toast.makeText(this, getString(R.string.room_closed_toast), Toast.LENGTH_SHORT).show()
            }

        }

        dialog.show()
    }

    override fun onBackPressed() {
        if (currentFragment == Constants.past_tag){
            val fragmentHistory = FragmentHistory()
            val fragmentBundle = Bundle();
            fragmentBundle.putString(Constants.room_uid, roomUID)
            fragmentHistory.arguments = fragmentBundle

            setFragment(fragmentHistory, Constants.history_tag)
            currentFragment = Constants.history_tag
        }
        else{
            super.onBackPressed()

        }
    }

    private fun shareRoom(){
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())


        val url = Constants.base_join_url + room.uid
        val message = "${firebaseUser.displayName} Invites you to join a room on ezBalans.\n\n$url"
        val image = getImageUri()

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_TITLE, "Invitation link to ${room.name}")
            putExtra(Intent.EXTRA_STREAM, image)
            data = image
            type = "text/plain"

        }
        startActivity(Intent.createChooser(shareIntent, "Share with:"))
    }

    private fun getImageUri(): Uri {
        var uri = Uri.EMPTY

        Picasso.get().load(room.image).into(object: Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                val file = File(applicationContext.externalCacheDir, "share_image_" + System.currentTimeMillis() + ".png")
                val out = FileOutputStream(file)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 70 , out)
                out.close()
                uri = Uri.fromFile(file)

            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }

        })
        return uri
    }


}
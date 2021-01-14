package com.ezbalans.app.ezbalans.Mainframe_Fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.Adapters.MyRoomsAdapter
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.Helpers.CreateNotification
import com.ezbalans.app.ezbalans.Helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.Models.Room
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.Rooms.CreateRoom
import com.ezbalans.app.ezbalans.Rooms.RoomActivity
import com.ezbalans.app.ezbalans.Rooms.ShoppingList
import com.ezbalans.app.ezbalans.databinding.DialogSendEmailVerificationBinding
import com.ezbalans.app.ezbalans.databinding.FragmentRoomsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference


class FragmentRooms: Fragment(), MyRoomsAdapter.OnItemClickListener {
    private var _binding: FragmentRoomsBinding? = null
    private val binding get() = _binding!!

    private val databaseReference = Firebase.database.reference
    val firebaseUser = Firebase.auth.currentUser!!
    val myRooms = arrayListOf<Room>()
    val myRoomsKeys = arrayListOf<String>()

    private lateinit var adapter: MyRoomsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRoomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MyRoomsAdapter(requireContext(), myRooms, this)

        // deep-link join loading
        val roomUid = arguments?.getString(Constants.room_uid)
        if (roomUid != null){
            jointFromDeepLink(roomUid)
        }
        else {
            // regular loading
            getMyRooms()
        }

        val lang = PowerPreference.getDefaultFile().getString(Constants.language)
        if (lang == Constants.language_hebrew){
            binding.fabHeb.visibility = View.VISIBLE
            binding.fab.visibility = View.GONE

            binding.joinRoomHeb.setOnClickListener{
                joinRoomDialog()
            }

            binding.createRoomHeb.setOnClickListener{
                // user can not create a room before email verification
                if (firebaseUser.isEmailVerified){
                    val intent = Intent(context, CreateRoom::class.java)
                    startActivity(intent)
                    binding.fab.close(true)
                }
                else {
                    openNotVerifiedDialog()
                }
            }
        }

        else {
            binding.fabHeb.visibility = View.GONE
            binding.fab.visibility = View.VISIBLE

            binding.joinRoom.setOnClickListener{
                joinRoomDialog()
            }

            binding.createRoom.setOnClickListener{
                // user can not create a room before email verification
                if (firebaseUser.isEmailVerified){
                    val intent = Intent(context, CreateRoom::class.java)
                    startActivity(intent)
                    binding.fab.close(true)
                }
                else {
                    openNotVerifiedDialog()
                }
            }
        }


        binding.joinRoom.setOnClickListener{
            joinRoomDialog()
        }

        binding.createRoom.setOnClickListener{
            // user can not create a room before email verification
            if (firebaseUser.isEmailVerified){
                val intent = Intent(context, CreateRoom::class.java)
                startActivity(intent)
                binding.fab.close(true)
            }
            else {
                openNotVerifiedDialog()
            }
        }
    }

    private fun openNotVerifiedDialog(){
        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_send_email_verification).create()
        val sendEmail = dialog.findViewById<Button>(R.id.send_email)


        sendEmail.setOnClickListener {
            firebaseUser.sendEmailVerification().addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(requireContext(), getString(R.string.verification_email_sent), Toast.LENGTH_SHORT).show()
                }
                else {
                    Log.d("TAG", "openNotVerifiedDialog: ${it.result}")
                }
                dialog.dismiss()
            }
        }
        dialog.show()

    }

    private fun joinRoomDialog(){

        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_join_room).create()
        val identityKey = dialog.findViewById<TextView>(R.id.identity)
        val sendRequest = dialog.findViewById<Button>(R.id.send_request)

        sendRequest.setOnClickListener {
            val roomIdentityKey = identityKey.text.toString()
            if (!myRoomsKeys.contains(roomIdentityKey)){
                requestJoinRoom(roomIdentityKey, dialog)

            }
            else {
                dialog.dismiss()
                Toast.makeText(requireContext(), getString(R.string.already_resident), Toast.LENGTH_SHORT).show()

            }
        }
        dialog.show()

        binding.fab.close(true)
    }

    private fun requestJoinRoom(identityKey: String, dialog: Dialog){
        databaseReference.child(Constants.rooms).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (entry in snapshot.children) {
                    val room = entry.getValue<Room>()!!
                    if (room.identity_key == identityKey){
                        val status = room.residents[firebaseUser.uid]
                        if (!room.residents.containsKey(firebaseUser.uid) || !(status == Constants.requested || status == Constants.added || status == Constants.declined)){
                            databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(firebaseUser.uid).setValue(Constants.requested).addOnSuccessListener {
                                CreateNotification().create(room, Constants.notify_user_requested, firebaseUser.uid, room.uid, "")
                                dialog.dismiss()
                                Toast.makeText(requireContext(),getString(R.string.request_sent), Toast.LENGTH_SHORT).show()
                            }


                        }
                        else {
                                Toast.makeText(requireContext(),getString(R.string.cant_send_request), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (dialog.isShowing){
                    dialog.dismiss()
                    Toast.makeText(requireContext(),getString(R.string.cant_join_room), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun getMyRooms(){
        myRooms.clear()
        myRoomsKeys.clear()
        databaseReference.child(Constants.rooms).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (rooms in snapshot.children){
                    val room = rooms.getValue<Room>()!!
                    if (room.residents.containsKey(firebaseUser.uid)){
                        if (room.residents[firebaseUser.uid] == Constants.added){
                            myRooms.add(room)
                            myRoomsKeys.add(room.identity_key)
                        }
                    }
                }

                if (myRooms.isNotEmpty()){
                    if (isAdded)
                        updateRooms()
                }
                else{
                    binding.list.visibility = View.GONE
                    binding.emptyItem.visibility = View.VISIBLE
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun updateRooms(){
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context)

    }

    override fun onItemClick(position: Int) {
        val room = myRooms[position]
        val intent = Intent(context, RoomActivity::class.java)
        intent.putExtra(Constants.room_uid, room.uid)
        intent.putExtra(Constants.fragmentSelector, Constants.status_tag)
        startActivity(intent)
    }

    override fun onCartClick(position: Int) {
        val room = myRooms[position]
        val intent = Intent(context, ShoppingList::class.java)
        intent.putExtra(Constants.room_uid, room.uid)
        intent.putExtra(Constants.shopping_list, room.shopping_list)
        startActivity(intent)
    }

    override fun onDetailsClick(position: Int) {
        val room = myRooms[position]
        val intent = Intent(context, RoomActivity::class.java)
        intent.putExtra(Constants.room_uid, room.uid)
        intent.putExtra(Constants.fragmentSelector, Constants.details_tag)
        startActivity(intent)
    }

    override fun onEditClick(position: Int) {
        val room = myRooms[position]
        var currentBudget: Int = 0
        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_edit_room_budget).create()
        val budget = dialog.findViewById<EditText>(R.id.budget)
        val button = dialog.findViewById<Button>(R.id.apply)


        databaseReference.child(Constants.budgets).child(firebaseUser.uid).child(room.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    currentBudget = snapshot.getValue<Int>()!!
                }
                budget.setText(currentBudget.toString())
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        button.setOnClickListener {
            val newBudget = budget.text.toString().toInt()
            if (newBudget != currentBudget){
                databaseReference.child(Constants.budgets).child(firebaseUser.uid).child(room.uid).setValue(newBudget).addOnSuccessListener {
                    getMyRooms()
                    dialog.dismiss()
                }
            }

        }

        dialog.show()

    }

    private fun jointFromDeepLink(roomUid: String){
        databaseReference.child(Constants.rooms).child(roomUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue<Room>()!!
                if (room.residents.containsKey(firebaseUser.uid)){
                    if (room.residents[firebaseUser.uid] == Constants.removed || room.residents[firebaseUser.uid] == Constants.declined){
                        Toast.makeText(requireContext(),getString(R.string.cant_join_room), Toast.LENGTH_SHORT).show()
                        getMyRooms()
                    }

                    else if (room.residents[firebaseUser.uid] == Constants.added){
                        Toast.makeText(requireContext(),getString(R.string.already_resident), Toast.LENGTH_SHORT).show()
                        getMyRooms()

                    }
                    else {
                        databaseReference.child(Constants.rooms).child(roomUid).child(Constants.residents).child(firebaseUser.uid).setValue(Constants.added).addOnSuccessListener {
                            Toast.makeText(requireContext(),getString(R.string.you_joined_room), Toast.LENGTH_SHORT).show()
                            getMyRooms()
                        }
                    }
                }
                else {
                    databaseReference.child(Constants.rooms).child(roomUid).child(Constants.residents).child(firebaseUser.uid).setValue(Constants.added).addOnSuccessListener {
                        Toast.makeText(requireContext(),getString(R.string.you_joined_room), Toast.LENGTH_SHORT).show()
                        getMyRooms()
                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}
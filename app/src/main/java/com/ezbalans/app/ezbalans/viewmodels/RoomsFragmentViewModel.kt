package com.ezbalans.app.ezbalans.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.eventBus.RoomsEvent
import com.ezbalans.app.ezbalans.models.Room
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference
import org.greenrobot.eventbus.EventBus

class RoomsFragmentViewModel: ViewModel() {
    private val databaseReference = Firebase.database.reference

    private val rooms: MutableLiveData<List<Room>> by lazy {
        MutableLiveData()
    }

    fun getRooms() : LiveData<List<Room>>{
       return rooms
    }

    fun loadUsers() {
//        databaseReference.child(Constants.rooms).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (entry in snapshot.children) {
//                    val room = entry.getValue<Room>()!!
//                    rooms[room.uid] = room
//                    if (room.residents.containsKey(firebaseUser.uid)) {
//                        myRooms[room.uid] = room
//                    }
//                }
//                PowerPreference.getDefaultFile().setObject(Constants.rooms, rooms)
//                PowerPreference.getDefaultFile().setObject(Constants.my_rooms, myRooms)
//                fragmentCount += 1
//                setMainFragment()
//
//                EventBus.getDefault().post(RoomsEvent())
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })
    }
}
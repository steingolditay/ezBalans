package com.ezbalans.app.ezbalans.viewmodels.roomActivities

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoomInfoActivityViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    var roomUid = ""

    val myRoom : LiveData<Room> = Transformations.map(repository.provideMyRooms()) { data ->
        getMyRoom(data)
    }


    fun getAllUsers(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()
    }

    private fun getMyRoom(data: List<Room>): Room {
        var myRoom = Room()
        for (room in data){
            if (room.uid == roomUid){
                myRoom = room
                break
            }
        }
        return myRoom
    }

}
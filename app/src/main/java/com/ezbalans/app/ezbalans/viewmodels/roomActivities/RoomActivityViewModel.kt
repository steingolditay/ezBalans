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
class RoomActivityViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    var roomUid = ""

    val myRoom : LiveData<Room> = Transformations.map(repository.provideMyRooms()) { data ->
        getMyRoom(data)
    }

    val allUsers: LiveData<List<User>> = Transformations.map(repository.provideAllUsers()) { data ->
        getAllUsers(data)
    }



    fun addUserToRoom(user: String, room: Room){
        return repository.addUserToRoom(user, room)

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

    private fun getAllUsers(data: HashMap<String, User>): List<User>{
        return data.values.toList()
    }

}
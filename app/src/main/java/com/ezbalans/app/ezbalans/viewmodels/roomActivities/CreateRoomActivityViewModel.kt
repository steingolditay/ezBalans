package com.ezbalans.app.ezbalans.viewmodels.roomActivities

import android.app.Dialog
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateRoomActivityViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    fun getAllUsers(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()
    }

    fun getAllRoomKeys(): LiveData<List<String>> {
        return repository.provideAllRoomKeys()
    }

    fun createRoom(context: Context, room: Room){
       return repository.roomCreatedUpdate(context, room)

    }

}
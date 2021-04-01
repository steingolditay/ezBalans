package com.ezbalans.app.ezbalans.viewmodels.roomActivities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoomInfoActivityViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    fun getAllUsers(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()
    }

    fun getAllRooms(): LiveData<List<Room>> {
        return repository.provideAllRooms()
    }

}
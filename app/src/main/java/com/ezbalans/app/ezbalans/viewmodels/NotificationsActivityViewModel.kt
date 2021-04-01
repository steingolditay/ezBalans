package com.ezbalans.app.ezbalans.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationsActivityViewModel

@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    fun getAllUsers(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()
    }

    fun getMyNotifications(): LiveData<HashMap<String, Notification>> {
        return repository.provideMyNotifications()
    }

    fun getAllRooms(): LiveData<List<Room>> {
       return repository.provideAllRooms()
    }

}
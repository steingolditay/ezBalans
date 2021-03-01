package com.ezbalans.app.ezbalans.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository

class NotificationsActivityViewModel : ViewModel() {

    lateinit var repository: DatabaseRepository
    lateinit var myRooms: MutableLiveData<List<Room>>

    fun init(){
        if (this::myRooms.isInitialized){
            return
        }
        repository = DatabaseRepository
    }

    fun getAllUsers(): LiveData<HashMap<String, User>> {
        return repository.getAllUsers()
    }

    fun getMyNotifications(): LiveData<HashMap<String, Notification>> {
        return repository.getMyNotifications()
    }

    fun getAllRooms(): LiveData<List<Room>> {
       return repository.getAllRooms()
    }

}
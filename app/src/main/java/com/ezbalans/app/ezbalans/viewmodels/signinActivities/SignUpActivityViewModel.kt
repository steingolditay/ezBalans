package com.ezbalans.app.ezbalans.viewmodels.signinActivities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.repository.DatabaseRepository

class SignUpActivityViewModel : ViewModel() {

    lateinit var repository: DatabaseRepository
    lateinit var myRooms: MutableLiveData<List<Room>>

    fun init(){
        if (this::myRooms.isInitialized){
            return
        }
        repository = DatabaseRepository
    }

    fun getUserKeys(): LiveData<List<String>> {
        return repository.getUserKeys()
    }
//
//    fun getAllRoomKeys(): LiveData<List<String>> {
//        return repository.getAllRoomKeys()
//    }

}
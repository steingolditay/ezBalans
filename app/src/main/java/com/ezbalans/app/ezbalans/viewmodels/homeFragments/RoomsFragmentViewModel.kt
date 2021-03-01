package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.repository.DatabaseRepository

class RoomsFragmentViewModel : ViewModel() {

    lateinit var repository: DatabaseRepository
    lateinit var myRooms: MutableLiveData<List<Room>>

    fun init(){
        if (this::myRooms.isInitialized){
            return
        }
        repository = DatabaseRepository
//        myRooms = repository.getMyRooms()
    }

    fun getMyRooms(): LiveData<List<Room>> {
        return repository.getMyRooms()
    }

    fun getMyNotifications(): LiveData<HashMap<String, Notification>> {
        return repository.getMyNotifications()
    }


}
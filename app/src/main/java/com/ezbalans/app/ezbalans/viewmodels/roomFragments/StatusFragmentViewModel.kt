package com.ezbalans.app.ezbalans.viewmodels.roomFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository

class StatusFragmentViewModel : ViewModel() {

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

    fun getMyRooms(): LiveData<List<Room>> {
        return repository.getMyRooms()
    }

    fun getAllPayments(): LiveData<HashMap<String, Payment>> {
       return repository.getAllPayments()
    }

}
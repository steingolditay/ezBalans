package com.ezbalans.app.ezbalans.viewmodels.roomActivities

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository

class ShoppingListActivityViewModel : ViewModel() {

    lateinit var repository: DatabaseRepository
    lateinit var myRooms: MutableLiveData<List<Room>>

    fun init(){
        if (this::myRooms.isInitialized){
            return
        }
        repository = DatabaseRepository
    }

    fun getShoppingList(): LiveData<HashMap<String, HashMap<String, Boolean>>> {
        return repository.getMyShoppingLists()
    }

    fun getAllRooms(): LiveData<List<Room>> {
        return repository.getAllRooms()
    }

}
package com.ezbalans.app.ezbalans.viewmodels.roomActivities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShoppingListActivityViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {


    fun getShoppingList(): LiveData<HashMap<String, HashMap<String, Boolean>>> {
        return repository.provideMyShoppingLists()
    }

    fun getAllRooms(): LiveData<List<Room>> {
        return repository.provideAllRooms()
    }

}
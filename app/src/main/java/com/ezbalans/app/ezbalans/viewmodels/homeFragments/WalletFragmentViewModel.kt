package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.repository.DatabaseRepository

class WalletFragmentViewModel : ViewModel() {

    lateinit var repository: DatabaseRepository
    lateinit var myRooms: MutableLiveData<List<Room>>

    fun init(){
        if (this::myRooms.isInitialized){
            return
        }
        repository = DatabaseRepository
    }

    fun getMyBudgets(): LiveData<HashMap<String, Int>> {
        return repository.getMyBudgets()
    }

    fun getMyRooms(): LiveData<List<Room>> {
        return repository.getMyRooms()
    }

    fun getMyPayments(): LiveData<HashMap<String, Payment>> {
       return repository.getMyPayments()
    }

}
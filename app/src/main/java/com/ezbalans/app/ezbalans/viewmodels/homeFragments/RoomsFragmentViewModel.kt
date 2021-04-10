package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoomsFragmentViewModel

@Inject constructor(private val repository: DatabaseRepository): ViewModel(){




    fun getMyRooms(): LiveData<List<Room>> {
        return repository.provideMyRooms()
    }

    fun getMyPayments(): LiveData<HashMap<String, Payment>> {
        return repository.provideMyPayments()
    }

    fun getMyBudgets(): LiveData<HashMap<String, Int>> {
        return repository.provideMyBudgets()
    }

    fun getMyNotifications(): LiveData<HashMap<String, Notification>> {
        return repository.provideMyNotifications()
    }


}
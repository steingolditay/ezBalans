package com.ezbalans.app.ezbalans.viewmodels.roomFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsFragmentViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    fun getAllUsers(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()
    }

    fun getMyRooms(): LiveData<List<Room>> {
        return repository.provideMyRooms()
    }

    fun getMyPayments(): LiveData<HashMap<String, Payment>> {
       return repository.provideMyPayments()
    }

}
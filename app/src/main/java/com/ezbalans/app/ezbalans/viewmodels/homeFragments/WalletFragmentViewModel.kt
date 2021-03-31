package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletFragmentViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    private val paymentsMap = MutableLiveData<HashMap<String, Payment>>()

    fun getMyBudgets(): LiveData<HashMap<String, Int>> {
        return repository.provideMyBudgets()
    }

    fun getMyRooms(): LiveData<List<Room>> {
        return repository.provideMyRooms()
    }

    fun getMyPayments(): LiveData<HashMap<String, Payment>> {
        paymentsMap.postValue(repository.provideMyPayments().value)
        return paymentsMap
    }

}
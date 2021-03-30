package com.ezbalans.app.ezbalans.viewmodels.signinActivities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpActivityViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    fun getUserKeys(): LiveData<List<String>> {
        return repository.provideMyRoomKeys()
    }


}
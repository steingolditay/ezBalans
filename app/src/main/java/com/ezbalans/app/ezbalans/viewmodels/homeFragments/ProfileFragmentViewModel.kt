package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileFragmentViewModel

@Inject constructor(private val repository: DatabaseRepository) : ViewModel() {

    private var myUser = MutableLiveData<HashMap<String, User>>()


    fun getMyUser(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()

    }


}
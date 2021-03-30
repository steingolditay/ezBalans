package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileFragmentViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    private var myUser =  MutableLiveData<HashMap<String, User>>()


    fun getMyUser(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()
    }




}
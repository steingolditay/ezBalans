package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileFragmentViewModel

@Inject constructor(private val repository: DatabaseRepository) : ViewModel() {


    val myUser : LiveData<User> = Transformations.map(repository.provideAllUsers()) { data ->
        getMyUser(data)
    }



    fun logoutRepository(){
        return repository.removeListeners()
    }

    private fun getMyUser(data: HashMap<String, User>): User {
        return data[Firebase.auth.currentUser!!.uid]!!
    }
}
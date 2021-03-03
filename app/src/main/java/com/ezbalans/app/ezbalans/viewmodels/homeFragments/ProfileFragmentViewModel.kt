package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragmentViewModel : ViewModel() {

    lateinit var repository: DatabaseRepository
    lateinit var myRooms: MutableLiveData<List<Room>>
    lateinit var myUser: MutableLiveData<User>

    fun init(){
        if (this::repository.isInitialized && this::myUser.isInitialized){
            return
        }
        repository = DatabaseRepository
        myUser = MutableLiveData<User>()

    }


    fun getMyUser(): LiveData<User> {
        val users = repository.getAllUsers().value!!
        myUser.postValue(users[Firebase.auth.currentUser!!.uid])
        return myUser
    }




}
package com.ezbalans.app.ezbalans.viewmodels.signinActivities

import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginActivityViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    fun loginRepository(){
        return repository.addListeners()

    }


}
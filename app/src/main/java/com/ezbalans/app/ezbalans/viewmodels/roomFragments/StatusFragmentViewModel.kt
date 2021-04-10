package com.ezbalans.app.ezbalans.viewmodels.roomFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import com.ezbalans.app.ezbalans.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@HiltViewModel
class StatusFragmentViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    var roomUid = ""

    val myRoom : LiveData<Room> = Transformations.map(repository.provideMyRooms()) { data ->
        getMyRoom(data)
    }

    val roomResidentsList: LiveData<List<User>> = Transformations.map(repository.provideAllUsers()) { data ->
        getRoomResidents(data)
    }

    val roomPaymentsList: LiveData<List<Payment>> = Transformations.map(repository.provideAllPayments()) { data ->
        getRoomPayments(data)
    }


    fun getAllUsers(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()
    }



    private fun getMyRoom(data: List<Room>): Room {
        var myRoom = Room()
        for (room in data){
            if (room.uid == roomUid){
                myRoom = room
                break
            }
        }
        return myRoom
    }

    private fun getRoomResidents(users: HashMap<String, User>): List<User>{
        val list = arrayListOf<User>()

        for (resident in myRoom.value!!.residents.keys){
            list.add(users[resident]!!)
        }
        return list.toList()
    }

    private fun getRoomPayments(data: HashMap<String, Payment>): List<Payment>{
        val list = ArrayList<Payment>()
        for (payment in data.values){
            if (payment.to == roomUid && payment.status == Constants.payment_valid && paymentFromThisMonth(payment)){
                list.add(payment)

            }
        }

        return  list
    }

    private fun paymentFromThisMonth(payment: Payment) : Boolean{
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val thisMonth = calendar.get(Calendar.MONTH) + 1
        val thisYear = calendar.get(Calendar.YEAR)
        calendar.timeInMillis = payment.timestamp.toLong()
        val paymentMonth = calendar.get(Calendar.MONTH) +1
        val paymentYear = calendar.get(Calendar.YEAR)

        return (thisMonth == paymentMonth) && (paymentYear == thisYear)
    }

}


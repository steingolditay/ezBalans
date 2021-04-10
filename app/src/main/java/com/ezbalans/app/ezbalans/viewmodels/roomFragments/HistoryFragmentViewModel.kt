package com.ezbalans.app.ezbalans.viewmodels.roomFragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import com.ezbalans.app.ezbalans.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@HiltViewModel
class HistoryFragmentViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    var roomUid = ""


    val myRoom : LiveData<Room> = Transformations.map(repository.provideMyRooms()) { data ->
        getMyRoom(data)
    }

    val roomPaymentsList: LiveData<List<Payment>> = Transformations.map(repository.provideAllPayments()) { data ->
        getRoomPayments(data)
    }

    fun getAllUsers(): LiveData<HashMap<String, User>> {
        return repository.provideAllUsers()
    }


    fun getMyPayments(): LiveData<HashMap<String, Payment>> {
       return repository.provideMyPayments()
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

    private fun getRoomPayments(data: HashMap<String, Payment>): List<Payment>{
        val list = ArrayList<Payment>()
        for (payment in data.values){
            if (payment.to == roomUid && payment.status == Constants.payment_valid && isPaymentFromPastMonth(payment)){
                list.add(payment)

            }
        }
        return  list
    }

    private fun isPaymentFromPastMonth(payment: Payment) : Boolean{
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val thisMonth = calendar.get(Calendar.MONTH) + 1
        val thisYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = payment.timestamp.toLong()
        val paymentMonth = calendar.get(Calendar.MONTH) +1
        val paymentYear = calendar.get(Calendar.YEAR)

        return (((thisYear == paymentYear) && (paymentMonth < thisMonth) ) || (paymentYear < thisYear))
    }

}
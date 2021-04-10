package com.ezbalans.app.ezbalans.viewmodels.homeFragments

import androidx.lifecycle.*
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletFragmentViewModel
@Inject constructor(private val repository: DatabaseRepository): ViewModel() {

    private val roomPaymentsList = HashMap<String, HashMap<String, Payment>>()



    val myRoomPaymentsList: LiveData<HashMap<String, HashMap<String, Payment>>> = Transformations.map(repository.provideMyPayments()) {data ->
        getRoomPaymentsList(data)
    }

    val myPaymentsList: LiveData<List<Payment>> = Transformations.map(repository.provideMyPayments()) { data ->
        getPaymentList(data)
    }

    val myRoomsList: LiveData<Map<String, Room>> = Transformations.map(repository.provideMyRooms()) { data ->
        getMyRoomsList(data)
    }


    fun getMyBudgets(): LiveData<HashMap<String, Int>> {
        return repository.provideMyBudgets()
    }

    private fun getMyRoomsList(data: List<Room>): Map<String, Room> {
        return data.associateBy { it.uid }
    }

    private fun getPaymentList(data: HashMap<String, Payment>): List<Payment>{
        return data.values.toList()
    }

    private fun getRoomPaymentsList (data: HashMap<String, Payment>): HashMap<String, HashMap<String, Payment>>{
        viewModelScope.launch(Dispatchers.Default) {
            roomPaymentsList.clear()

            for (payment in data.values){
                if (roomPaymentsList.containsKey(payment.to)) {
                    roomPaymentsList[payment.to]!![payment.payment_uid] = payment
                }
                else {
                    val paymentHash = hashMapOf<String, Payment>()
                    paymentHash[payment.payment_uid] = payment
                    roomPaymentsList[payment.to] = paymentHash
                }
            }
        }

        return roomPaymentsList
    }

}
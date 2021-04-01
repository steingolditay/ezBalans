package com.ezbalans.app.ezbalans.views.rooms.roomFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.adapters.RoomHistoryAdapter
import com.ezbalans.app.ezbalans.utils.Constants
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.views.rooms.roomActivities.RoomActivity
import com.ezbalans.app.ezbalans.databinding.FragmentRoomHistoryBinding
import com.ezbalans.app.ezbalans.viewmodels.roomFragments.HistoryFragmentViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class FragmentHistory: Fragment(), RoomHistoryAdapter.OnItemClickListener{

    private var _binding: FragmentRoomHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryFragmentViewModel by viewModels()

    val payments = arrayListOf<Payment>()
    private val roomUsers = arrayListOf<User>()
    private var allUsers = HashMap<String, User>()
    private val pastPayments = HashMap<Long, Int>()
    var keys = arrayListOf<Long>()
    val firebaseUser = Firebase.auth.currentUser!!

    var room = Room()
    var roomUid: String = ""

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoomHistoryBinding.inflate(inflater, container, false)
        roomUid = arguments?.getString(Constants.room_uid)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }


    private fun initViewModel(){

        viewModel.getAllUsers().observe(viewLifecycleOwner, { users ->
            allUsers = users
        })

        viewModel.getMyRooms().observe(viewLifecycleOwner, {
            for (roomObject in it){
                if (roomObject.uid == roomUid){
                    room = roomObject
                    roomUsers.clear()
                    for (resident in room.residents.keys){
                        if (allUsers.containsKey(resident)){
                            val user = allUsers[resident]!!
                            roomUsers.add(user)
                        }

                    }
                }
            }
        })

        viewModel.getMyPayments().observe(viewLifecycleOwner, { paymentsList ->
            payments.clear()
            pastPayments.clear()

            for (payment in paymentsList.values){
                if (payment.to == room.uid && payment.status == Constants.payment_valid && isPaymentFromPastMonth(payment)){
                    putPaymentsDates(payment)
                }
            }

        })

        if (payments.isEmpty()) {
            binding.historyList.visibility = View.GONE
            binding.emptyItem.visibility = View.VISIBLE
        } else {
            binding.historyList.visibility = View.VISIBLE
            binding.emptyItem.visibility = View.GONE

            // sort list by timestamps
            payments.sortWith { obj1, obj2 -> obj1.timestamp.compareTo(obj2.timestamp) }

            getPaymentsPerMonth()
        }
    }


    private fun putPaymentsDates(payment: Payment){
        val calendar = Calendar.getInstance(TimeZone.getDefault())

        calendar.timeInMillis = payment.timestamp.toLong()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val timestamp = calendar.time.time

        payments.add(payment)
        pastPayments[timestamp] = 0
    }

    private fun getPaymentsPerMonth(){
        val paymentCalendar = Calendar.getInstance(TimeZone.getDefault())
        val timestampCalendar = Calendar.getInstance(TimeZone.getDefault())

        // list the actual payments in relation
        // to the found month/year timestamps
        for (entry in pastPayments){
            val timestamp = entry.key
            var totalAmount  = 0
            timestampCalendar.timeInMillis = timestamp

            for (p in payments){
                paymentCalendar.timeInMillis = p.timestamp.toLong()
                when {
                    (paymentCalendar.get(Calendar.YEAR) == timestampCalendar.get(Calendar.YEAR)) and (paymentCalendar.get(Calendar.MONTH) == timestampCalendar.get(Calendar.MONTH))->{
                        totalAmount += p.amount.toInt()
                    }
                }
            }
            pastPayments[timestamp] = totalAmount
        }


        loadData()
    }

    private fun isPaymentFromPastMonth(payment: Payment) : Boolean{
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val thisMonth = calendar.get(Calendar.MONTH) + 1
        val thisYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = payment.timestamp.toLong()
        val paymentMonth = calendar.get(Calendar.MONTH) +1
        val paymentYear = calendar.get(Calendar.YEAR)

        return (((thisYear == paymentYear) && (paymentMonth < thisMonth) ) || (paymentYear<thisYear))
    }

    private fun loadData(){
        val sortedMap = pastPayments.toSortedMap(compareByDescending { it })
        keys = ArrayList(sortedMap.keys)

        val details = HashMap<String, String>()
        details[Constants.room_uid] = roomUid
        details[Constants.uid] = firebaseUser.uid

        val adapter = RoomHistoryAdapter(sortedMap, keys, room.currency, this)

        binding.historyList.adapter = adapter
        binding.historyList.layoutManager = LinearLayoutManager(context)
        binding.historyList.setHasFixedSize(true)

    }

    override fun onItemClick(position: Long) {
        val fragmentBundle = Bundle()
        fragmentBundle.putString(Constants.room_uid, room.uid)
        fragmentBundle.putLong(Constants.time_stamp, position)

        val fragmentPastMonth = FragmentPastMonth()
        fragmentPastMonth.arguments = fragmentBundle

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainframe, fragmentPastMonth , Constants.past_tag)
        fragmentTransaction.commit()

        RoomActivity.currentFragment = Constants.past_tag
    }

}
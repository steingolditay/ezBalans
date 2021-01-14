package com.ezbalans.app.ezbalans.Rooms.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.Adapters.RoomHistoryAdapter
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.Models.Payment
import com.ezbalans.app.ezbalans.Models.Room
import com.ezbalans.app.ezbalans.Models.User
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.Rooms.RoomActivity
import com.ezbalans.app.ezbalans.databinding.FragmentRoomHistoryBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FragmentHistory: Fragment(), RoomHistoryAdapter.OnItemClickListener{

    private var _binding: FragmentRoomHistoryBinding? = null
    private val binding get() = _binding!!

    private val databaseReference = Firebase.database.reference

    val payments = arrayListOf<Payment>()
    val users = arrayListOf<User>()
    private val pastPayments = HashMap<Long, Int>()
    var keys = arrayListOf<Long>()
    val firebaseUser = Firebase.auth.currentUser!!


    var room = Room()
    var roomUid: String = ""
    var totalAmount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRoomHistoryBinding.inflate(inflater, container, false)

        roomUid = arguments?.getString(Constants.room_uid)!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadRoom()

    }

    private fun loadRoom(){
        databaseReference.child(Constants.rooms).child(roomUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                room = snapshot.getValue<Room>()!!
                loadRoomUsers()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun loadRoomUsers(){
        users.clear()
        databaseReference.child(Constants.users).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (room_user_uid in room.residents.keys){
                    val user = snapshot.child(room_user_uid).getValue<User>()!!
                    users.add(user)
                }

                loadPayments()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun loadPayments(){
        payments.clear()
        pastPayments.clear()
        keys.clear()
        val calendar = Calendar.getInstance()

        databaseReference.child(Constants.payments).child(roomUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (payment in snapshot.children) {
                    val p = payment.getValue<Payment>()!!
                    if (p.status == Constants.payment_valid && isPaymentFromPastMonth(p)) {

                        calendar.timeInMillis = p.timestamp.toLong()
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val timestamp = calendar.time.time

                        payments.add(p)
                        pastPayments[timestamp] = 0
                        Log.d("TAG", "loadData: $pastPayments //")

                    }
                }

                if (isAdded) {
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
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getPaymentsPerMonth(){
        val paymentCalendar = Calendar.getInstance()
        val timestampCalendar = Calendar.getInstance()

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
        val calendar = Calendar.getInstance()
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

        val adapter = RoomHistoryAdapter(requireContext(), sortedMap, keys, room.currency, this)

        binding.historyList.adapter = adapter
        binding.historyList.layoutManager = LinearLayoutManager(context)
        binding.historyList.setHasFixedSize(true)

    }

    override fun onItemClick(position: Long) {
        val fragmentBundle = Bundle();
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
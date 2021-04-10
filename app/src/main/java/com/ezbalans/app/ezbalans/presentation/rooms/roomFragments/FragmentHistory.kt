package com.ezbalans.app.ezbalans.presentation.rooms.roomFragments

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
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.presentation.rooms.roomActivities.RoomActivity
import com.ezbalans.app.ezbalans.databinding.FragmentRoomHistoryBinding
import com.ezbalans.app.ezbalans.viewmodels.roomFragments.HistoryFragmentViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class FragmentHistory: Fragment(), RoomHistoryAdapter.OnItemClickListener{

    private var _binding: FragmentRoomHistoryBinding? = null
    private val binding get() = _binding!!

    val firebaseUser = Firebase.auth.currentUser!!

    private var pastPayments = hashMapOf<Long, Int>()
    private val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
    private lateinit var payments: List<Payment>

    private lateinit var room: Room
    private lateinit var roomUid: String
    private lateinit var adapter: RoomHistoryAdapter

    private val viewModel: HistoryFragmentViewModel by viewModels()



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
        viewModel.roomUid = roomUid

        viewModel.myRoom.observe(viewLifecycleOwner, {
            room = it
            initData()
        })

        viewModel.roomPaymentsList.observe(viewLifecycleOwner, { result ->
            pastPayments.clear()
            payments = result
            for (payment in payments){
                preparePastMonthsPaymentDates(payment)
            }
            initData()
        })

    }

    private fun preparePastMonthsPaymentDates(payment: Payment){
        calendar.timeInMillis = payment.timestamp.toLong()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val timestamp = calendar.time.time

        if (pastPayments.containsKey(timestamp)){
            pastPayments[timestamp] = pastPayments[timestamp]!!.toInt() +  payment.amount.toInt()
        }
        else {
            pastPayments[timestamp] = payment.amount.toInt()

        }
    }

    private fun initData(){
        if (this::payments.isInitialized && this::room.isInitialized){
            if (payments.isNotEmpty()){
                binding.historyList.visibility = View.VISIBLE
                binding.emptyItem.visibility = View.GONE

                payments.sortedBy { it.timestamp }
                initRecyclerView()
            }
            else {
                binding.historyList.visibility = View.GONE
                binding.emptyItem.visibility = View.VISIBLE
            }
        }
    }


    private fun initRecyclerView(){
        val sortedMap = pastPayments.toSortedMap(compareByDescending { it })
        val keys = ArrayList(sortedMap.keys)

        adapter = RoomHistoryAdapter(sortedMap, keys, room.currency, this)
        binding.historyList.adapter = adapter
        binding.historyList.layoutManager = LinearLayoutManager(context)
        binding.historyList.setHasFixedSize(true)

    }

    override fun onItemClick(position: Long) {
        val fragmentBundle = Bundle()
        fragmentBundle.putString(Constants.room_uid, roomUid)
        fragmentBundle.putLong(Constants.time_stamp, position)

        val fragmentPastMonth = FragmentPastMonth()
        fragmentPastMonth.arguments = fragmentBundle

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainframe, fragmentPastMonth , Constants.past_tag)
        fragmentTransaction.commit()

        RoomActivity.currentFragment = Constants.past_tag
    }

}
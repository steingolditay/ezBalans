package com.ezbalans.app.ezbalans.views.rooms.roomFragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.adapters.RoomPaymentsAdapter
import com.ezbalans.app.ezbalans.helpers.Constants
import com.ezbalans.app.ezbalans.helpers.GetCurrentDate
import com.ezbalans.app.ezbalans.helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.helpers.TranslateToHebrew
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.FragmentRoomStatusBinding
import com.ezbalans.app.ezbalans.viewmodels.roomFragments.StatusFragmentViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference
import com.skydoves.powerspinner.PowerSpinnerView
import java.util.*
import kotlin.collections.HashMap

class FragmentStatus: Fragment(), RoomPaymentsAdapter.OnItemClickListener {
    private var _binding: FragmentRoomStatusBinding? = null
    private val binding get() = _binding!!

    private val firebaseUser = Firebase.auth.currentUser!!
    private val databaseReference = Firebase.database.reference

    lateinit var viewModel: StatusFragmentViewModel
    lateinit var roomUid: String


    val payments = arrayListOf<Payment>()
    private var allUsers = HashMap<String, User>()
    private val roomUsers = arrayListOf<User>()
    private val roomUsersMap = HashMap<String, User>()

    var room = Room()
    private val roomCategories = arrayListOf<String>()
    var totalAmount = 0
    lateinit var myContext: Context
    lateinit var adapter: RoomPaymentsAdapter


    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRoomStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.monthTitle.text = String.format("%s %s", GetCurrentDate().monthName(), GetCurrentDate().year())

        binding.fab.setOnClickListener{
                addPaymentDialog()
        }

        roomUid = arguments?.getString(Constants.room_uid)!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(StatusFragmentViewModel::class.java)
        viewModel.init()

        viewModel.getMyRooms().observe(requireActivity(), {
            for (roomObject in it){
                if (roomObject.uid == roomUid){
                    room = roomObject

                    updateBudget()

                    for (entry in room.categories){
                        when (entry.value){
                            true -> roomCategories.add(entry.key)
                        }
                    }
                }
            }
        })

        viewModel.getAllUsers().observe(requireActivity(), {
            roomUsers.clear()
            roomUsersMap.clear()
            allUsers = it

            for (uid in room.residents.keys) {
                Log.d("TAG", "onActivityCreated: $uid")
                Log.d("TAG", "onActivityCreated: $allUsers")
                val user = allUsers[uid]!!

                roomUsers.add(user)
                roomUsersMap[user.uid] = user
            }
        })

        viewModel.getAllPayments().observe(requireActivity(), {
            payments.clear()
            totalAmount = 0

            for (payment in it.values){
                if (payment.to == room.uid && payment.status == Constants.payment_valid && paymentFromThisMonth(payment)){
                    payments.add(payment)
                    totalAmount += payment.amount.toInt()
                }
            }
            if (payments.isEmpty()) {
                binding.list.visibility = View.GONE
                binding.emptyItem.visibility = View.VISIBLE
            } else {
                binding.list.visibility = View.VISIBLE
                binding.emptyItem.visibility = View.GONE

                // sort list by timestamps
                payments.sortWith { obj1, obj2 -> obj1.timestamp.compareTo(obj2.timestamp) }
                updatePayments()
            }

        })


    }


    private fun  addPaymentDialog(){
        val maximumDays = getMaximumDays()
        var paymentType = ""

        val dialog = GetCustomDialog(Dialog(myContext), R.layout.dialog_add_payment).create()
        val paymentTypeSpinner = dialog.findViewById<PowerSpinnerView>(R.id.payment_type)
        val addPayment = dialog.findViewById<Button>(R.id.add_payment)

        val amount = dialog.findViewById<EditText>(R.id.amount)
        val day = dialog.findViewById<EditText>(R.id.day)
        val description = dialog.findViewById<EditText>(R.id.description)


        val lang = PowerPreference.getDefaultFile().getString(Constants.language)
        if (lang == Constants.language_hebrew){
            val items = arrayListOf<String>()
            for (item in roomCategories){
                val translated = TranslateToHebrew().paymentCategory(item)
                items.add(translated)

            }
            paymentTypeSpinner.setItems(items)

        }
        else {
            paymentTypeSpinner.setItems(roomCategories)
        }
        paymentTypeSpinner.setOnSpinnerItemSelectedListener<String> { position, item ->

            paymentType = roomCategories[position]
        }

        addPayment.setOnClickListener {
            val amountInput = amount.text.toString().trim()
            val dayInput = day.text.toString().trim()
            val descInput = description.text.toString().trim()

            when {
                amountInput.isEmpty() -> {
                    amount.error = getString(R.string.amount_required)
                }
                amountInput == "0" -> {
                    amount.error = getString(R.string.amount_zero)
                }
                dayInput.isEmpty() -> {
                    day.error = getString(R.string.day_of_month_required)
                }
                ((dayInput.toInt()) > maximumDays) || dayInput.toInt() == 0 -> {
                    day.error = getString(R.string.invalid_day_of_month)
                }
                paymentType.isEmpty() -> {
                    paymentTypeSpinner.error = getString(R.string.payment_type_not_selected)
                }

                else -> {
                    val timestamp = getTimeStamp(dayInput.toInt())
                    val paymentUid = UUID.randomUUID().toString()
                    val payment = Payment(paymentUid, firebaseUser.uid, room.uid, amountInput, timestamp, descInput, Constants.payment_valid, paymentType)

                    databaseReference.child(Constants.payments).child(room.uid).child(paymentUid).setValue(payment).addOnSuccessListener {
                        dialog.dismiss()

                    }
                }
            }
        }

        dialog.show()
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

    private fun updateBudget(){
        val roomBudget = room.monthly_budget.toInt()
        binding.budget.text = String.format("%s / %s", totalAmount, roomBudget)

        when {
            roomBudget == 0 || totalAmount < roomBudget -> {
                binding.budget.setTextColor(resources.getColor(R.color.colorGreen, resources.newTheme()))
            }
            else -> {
                binding.budget.setTextColor(resources.getColor(R.color.colorPrimary, resources.newTheme()))
            }
        }
    }

    private fun updatePayments(){
        adapter = RoomPaymentsAdapter(requireContext(), payments, roomUsers, room.currency, this)
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context)
    }

    private fun getMaximumDays() : Int{
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getTimeStamp(day: Int) : String{
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        return (calendar.time.time).toString()

    }

    override fun onItemClick(position: Int) {
        val payment = payments[position]
        val user = roomUsersMap[payment.from]!!
        val currencySymbol = if (room.currency == Constants.nis) Constants.nis_symbol else Constants.usd_symbol

        val dialog = GetCustomDialog(Dialog(myContext), R.layout.dialog_payment_info).create()
        val userInfo = dialog.findViewById<TextView>(R.id.user_info)
        val amountInfo = dialog.findViewById<TextView>(R.id.amount_info)
        val dateInfo = dialog.findViewById<TextView>(R.id.date_info)
        val descriptionInfo = dialog.findViewById<TextView>(R.id.description_info)
        val categoryInfo = dialog.findViewById<TextView>(R.id.category_info)
        val deletePayment = dialog.findViewById<Button>(R.id.delete_payment)


        userInfo.text = user.username
        amountInfo.text = payment.amount + "$currencySymbol"
        dateInfo.text = GetCurrentDate().dateFromTimestamp(payment.timestamp.toLong())
        descriptionInfo.text = payment.description
        categoryInfo.text = payment.category

        if (user.uid == firebaseUser.uid){
            deletePayment.visibility = View.VISIBLE
            deletePayment.setOnClickListener {
                databaseReference.child(Constants.payments).child(room.uid).child(payment.payment_uid).removeValue().addOnSuccessListener {


                    dialog.dismiss()
                    Toast.makeText(context, getString(R.string.payment_deleted), Toast.LENGTH_SHORT).show()

                }
            }
        }


        dialog.show()
    }

}
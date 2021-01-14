package com.ezbalans.app.ezbalans.Rooms.Fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.Adapters.RoomPaymentsAdapter
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.Helpers.GetCurrentDate
import com.ezbalans.app.ezbalans.Helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.Helpers.TranslateToHebrew
import com.ezbalans.app.ezbalans.Models.Payment
import com.ezbalans.app.ezbalans.Models.Room
import com.ezbalans.app.ezbalans.Models.User
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.FragmentRoomStatusBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
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

    val payments = arrayListOf<Payment>()
    val users = arrayListOf<User>()
    val userList = HashMap<String, User>()

    var room = Room()
    val roomCategories = arrayListOf<String>()

    var totalAmount = 0
    lateinit var myContext: Context

    lateinit var adapter: RoomPaymentsAdapter



    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRoomStatusBinding.inflate(inflater, container, false)
        adapter = RoomPaymentsAdapter(requireContext(), payments, users, room.currency, this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.monthTitle.text = String.format("%s %s", GetCurrentDate().monthName(), GetCurrentDate().year())

        binding.fab.setOnClickListener{
            addPaymentDialog()
        }

        val roomUid = arguments?.getString(Constants.room_uid)!!
        loadRoom(roomUid)

    }

    private fun loadRoom(roomUid: String){
        roomCategories.clear()
        databaseReference.child(Constants.rooms).child(roomUid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                room = snapshot.getValue<Room>()!!
                for (entry in room.categories){
                    when (entry.value){
                       true -> roomCategories.add(entry.key)
                    }
                }
                loadRoomUsers()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun loadRoomUsers(){
        users.clear()
        userList.clear()
        databaseReference.child(Constants.users).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (room_user_uid in room.residents.keys){
                    val user = snapshot.child(room_user_uid).getValue<User>()!!
                    users.add(user)
                    userList[user.uid] = user
                }
                loadPayments()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun loadPayments(){
        payments.clear()
        totalAmount = 0

        databaseReference.child(Constants.payments).child(room.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (payment in snapshot.children) {
                    val p = payment.getValue<Payment>()!!
                    if (p.status == Constants.payment_valid && paymentFromThisMonth(p)) {
                        payments.add(p)
                        totalAmount += p.amount.toInt()
                    }
                }

                // prevent crash if moving to another fragment
                // while still loading data
                if (isAdded) {
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
                    updateBudget()

                }

            }

            override fun onCancelled(error: DatabaseError) {
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


//        roomCategories.sort()
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
                (dayInput.toInt()) > maximumDays -> {
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
                        loadPayments()

                    }
                }
            }
        }

        dialog.show()
    }

    private fun paymentFromThisMonth(payment: Payment) : Boolean{
        val calendar = Calendar.getInstance()
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
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context)
        binding.list.setHasFixedSize(true)
    }

    private fun getMaximumDays() : Int{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getTimeStamp(day: Int) : String{
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        return (calendar.time.time).toString()

    }

    override fun onItemClick(position: Int) {
        val payment = payments[position]
        val user = userList[payment.from]!!
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
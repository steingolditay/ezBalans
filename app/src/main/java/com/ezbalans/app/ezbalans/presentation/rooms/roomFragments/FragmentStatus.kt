package com.ezbalans.app.ezbalans.presentation.rooms.roomFragments

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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.adapters.RoomPaymentsAdapter
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.FragmentRoomStatusBinding
import com.ezbalans.app.ezbalans.utils.*
import com.ezbalans.app.ezbalans.viewmodels.roomFragments.StatusFragmentViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class FragmentStatus: Fragment(), RoomPaymentsAdapter.OnItemClickListener {
    private var _binding: FragmentRoomStatusBinding? = null
    private val binding get() = _binding!!

    private val firebaseUser = Firebase.auth.currentUser!!
    private val databaseReference = Firebase.database.reference

    private val viewModel: StatusFragmentViewModel by viewModels()

    lateinit var roomUid: String


    private var payments = listOf<Payment>()
    private var roomUsers = listOf<User>()
    private var roomUsersMap = mapOf<String, User>()

    var room = Room()
    private var roomCategories = arrayListOf<String>()
    private var totalAmount = 0
    private lateinit var myContext: Context
    lateinit var adapter: RoomPaymentsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoomStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.monthTitle.text = String.format("%s %s", DateAndTimeUtils().currentMonthName(), DateAndTimeUtils().currentYear())

        binding.fab.setOnClickListener{
                addPaymentDialog()
        }

        roomUid = arguments?.getString(Constants.room_uid)!!

        initViewModel()
    }

    private fun initViewModel(){
        viewModel.roomUid = roomUid

        viewModel.myRoom.observe(viewLifecycleOwner, {
            room = it
            roomCategories.clear()
            for (entry in room.categories){
                when (entry.value){
                    true -> roomCategories.add(entry.key)
                }
            }
        })

        viewModel.roomResidentsList.observe(viewLifecycleOwner, { result ->
            roomUsers = result
            roomUsersMap = result.associateBy { it.uid }
        })

        viewModel.roomPaymentsList.observe(viewLifecycleOwner, { result ->
            payments = result
            totalAmount = result.sumByDouble { it.amount.toDouble() }.toInt()
            updateBudget()
            initRecyclerView()

        })

    }

    private fun  addPaymentDialog(){
        val maximumDays = DateAndTimeUtils().currentMonthMaximumDays()
        var paymentType = ""

        val dialog = CustomDialog(Dialog(myContext), R.layout.dialog_add_payment).create()
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
        paymentTypeSpinner.setOnSpinnerItemSelectedListener<String> { position, _ ->

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
                    val timestamp = DateAndTimeUtils().flatTimestampForDayInThisMonth(dayInput.toInt())
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

    private fun initRecyclerView(){
        if (payments.isEmpty()) {
            binding.list.visibility = View.GONE
            binding.emptyItem.visibility = View.VISIBLE
        }
        else {
            binding.list.visibility = View.VISIBLE
            binding.emptyItem.visibility = View.GONE
            payments = payments.sortedBy { it.timestamp }

            adapter = RoomPaymentsAdapter(payments, roomUsers, room.currency, this)
            binding.list.adapter = adapter
            binding.list.layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onItemClick(position: Int) {
        val payment = payments[position]
        val user = roomUsersMap[payment.from]!!
        val currencySymbol = if (room.currency == Constants.nis) Constants.nis_symbol else Constants.usd_symbol

        val dialog = CustomDialog(Dialog(myContext), R.layout.dialog_payment_info).create()
        val userInfo = dialog.findViewById<TextView>(R.id.user_info)
        val amountInfo = dialog.findViewById<TextView>(R.id.amount_info)
        val dateInfo = dialog.findViewById<TextView>(R.id.date_info)
        val descriptionInfo = dialog.findViewById<TextView>(R.id.description_info)
        val categoryInfo = dialog.findViewById<TextView>(R.id.category_info)
        val deletePayment = dialog.findViewById<Button>(R.id.delete_payment)


        userInfo.text = user.username
        amountInfo.text = payment.amount + currencySymbol
        dateInfo.text = DateAndTimeUtils().dateFromCustomTimestamp(payment.timestamp.toLong())
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
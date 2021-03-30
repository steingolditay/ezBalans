package com.ezbalans.app.ezbalans.views.rooms.roomFragments

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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.adapters.RoomPaymentsAdapter
import com.ezbalans.app.ezbalans.helpers.Constants
import com.ezbalans.app.ezbalans.helpers.CreateNotification
import com.ezbalans.app.ezbalans.helpers.GetCurrentDate
import com.ezbalans.app.ezbalans.helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.FragmentRoomHistoryMonthBinding
import com.ezbalans.app.ezbalans.helpers.GetPrefs
import com.ezbalans.app.ezbalans.viewmodels.roomFragments.PastMonthFragmentViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.skydoves.powerspinner.PowerSpinnerView
import java.lang.StringBuilder
import java.util.*

class FragmentPastMonth: Fragment(), RoomPaymentsAdapter.OnItemClickListener {

    private var _binding: FragmentRoomHistoryMonthBinding? = null
    private val binding get() = _binding!!

    private val firebaseUser = Firebase.auth.currentUser!!
    private val databaseReference = Firebase.database.reference
    lateinit var viewModel: PastMonthFragmentViewModel

    val payments = arrayListOf<Payment>()
    private val roomUsers = arrayListOf<User>()
    private val roomUsersList = HashMap<String, User>()
    var allUsers = HashMap<String, User>()


    var room = Room()
    var totalAmount = 0
    lateinit var myContext: Context
    private var monthTimestamp: Long = 0
    val roomCategories = arrayListOf<String>()

    private lateinit var balanceChartCard: CardView
    private lateinit var balanceChart: PieChart
    private lateinit var debt: TextView
    private lateinit var breakEven: Button
    var currencySymbol = ""
    private lateinit var roomUid: String


    lateinit var adapter: RoomPaymentsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRoomHistoryMonthBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.monthTitle.text = String.format("%s %s", GetCurrentDate().customMonthName(monthTimestamp), GetCurrentDate().customYear(monthTimestamp))


        balanceChart = view.findViewById(R.id.balance_pie_chart)
        balanceChartCard = view.findViewById(R.id.balance_card)
        debt = view.findViewById(R.id.debt)
        breakEven = view.findViewById(R.id.break_even)

        binding.fab.setOnClickListener{
            addPaymentDialog()
        }

        monthTimestamp = arguments?.getLong(Constants.time_stamp)!!
        roomUid = arguments?.getString(Constants.room_uid)!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(PastMonthFragmentViewModel::class.java)

        viewModel.getAllUsers().observe(requireActivity(), {
            allUsers = it
        })

        viewModel.getMyRooms().observe(requireActivity(), {
            for (roomObject in it) {
                if (roomObject.uid == roomUid){
                    room = roomObject
                    currencySymbol = if (room.currency == Constants.nis) Constants.nis_symbol else Constants.usd_symbol

                    for (entry in room.categories){
                        when (entry.value){
                            true -> roomCategories.add(entry.key)
                        }
                    }

                    for (resident in room.residents.keys){
                        val user = allUsers[resident]!!
                        roomUsers.add(user)
                        roomUsersList[user.uid] = user
                    }

                }
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

            if (payments.isEmpty()){
                binding.list.visibility = View.GONE
                binding.emptyItem.visibility = View.VISIBLE
            }

            else {
                binding.list.visibility = View.VISIBLE
                binding.emptyItem.visibility = View.GONE

                // sort list by timestamps
                payments.sortWith { obj1, obj2 -> obj1.timestamp.compareTo(obj2.timestamp) }
                binding.budget.text = totalAmount.toString()
                createBalanceChart()
                updatePayments()
            }
        })

    }

    private fun paymentFromThisMonth(payment: Payment) : Boolean{
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = monthTimestamp
        val thisMonth = calendar.get(Calendar.MONTH) + 1
        val thisYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = payment.timestamp.toLong()
        val paymentMonth = calendar.get(Calendar.MONTH) +1
        val paymentYear = calendar.get(Calendar.YEAR)



        return (thisMonth == paymentMonth) && (paymentYear == thisYear)
    }

    private fun updatePayments(){
        adapter = RoomPaymentsAdapter(requireContext(), payments, roomUsers, room.currency, this)
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context)
        binding.list.setHasFixedSize(true)
    }

    private fun getMaximumDays() : Int{
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = monthTimestamp
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

    private fun  addPaymentDialog(){
        val maximumDays = getMaximumDays()
        var paymentType = ""

        val dialog = GetCustomDialog(Dialog(myContext), R.layout.dialog_add_payment).create()
        val paymentTypeSpinner = dialog.findViewById<PowerSpinnerView>(R.id.payment_type)
        val addPayment = dialog.findViewById<Button>(R.id.add_payment)

        val amount = dialog.findViewById<EditText>(R.id.amount)
        val day = dialog.findViewById<EditText>(R.id.day)
        val description = dialog.findViewById<EditText>(R.id.description)


        roomCategories.sort()
        paymentTypeSpinner.setItems(roomCategories)
        paymentTypeSpinner.setOnSpinnerItemSelectedListener<String> { _, item ->
            paymentType = item
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
                    val status = if (room.admins[firebaseUser.uid] == true) Constants.payment_valid else Constants.payment_invalid

                    val timestamp = getTimeStamp(dayInput.toInt())
                    val paymentUid = UUID.randomUUID().toString()
                    val payment = Payment(paymentUid, firebaseUser.uid, room.uid, amountInput, timestamp, descInput, status, paymentType)

                    databaseReference.child(Constants.payments).child(room.uid).child(paymentUid).setValue(payment).addOnSuccessListener {
                        if (status == Constants.payment_invalid){
                            CreateNotification().create(room, Constants.notify_payment_invalid, firebaseUser.uid, paymentUid, "")
                        }
                        dialog.dismiss()

                    }
                }
            }
        }

        dialog.show()
    }

    override fun onItemClick(position: Int) {
        val payment = payments[position]
        val user = roomUsersList[payment.from]!!

        val dialog = GetCustomDialog(Dialog(myContext), R.layout.dialog_payment_info).create()
        val userInfo = dialog.findViewById<TextView>(R.id.user_info)
        val amountInfo = dialog.findViewById<TextView>(R.id.amount_info)
        val dateInfo = dialog.findViewById<TextView>(R.id.date_info)
        val descriptionInfo = dialog.findViewById<TextView>(R.id.description_info)
        val categoryInfo = dialog.findViewById<TextView>(R.id.category_info)
        val deletePayment = dialog.findViewById<Button>(R.id.delete_payment)

        userInfo.text = user.username
        amountInfo.text = payment.amount
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

    private fun createBalanceChart(){
        val users = arrayListOf<String>()
        val usersExpenses = HashMap<String, Float>()

        // collect payments per type
        for (payment in payments){
            val user = payment.from
            when (users.contains(user)){
                true -> {
                    val currentValue = usersExpenses[user]!!
                    usersExpenses[user] = currentValue + payment.amount.toFloat()
                }
                false -> {
                    users.add(users.size, user)
                    usersExpenses[user] = payment.amount.toFloat()
                }
            }
        }

        users.sort()

        val sortedList = usersExpenses.toSortedMap()

        if (sortedList.isNotEmpty()){
            balanceChartCard.visibility = View.VISIBLE


            var i = 0
            val pieEntries = arrayListOf<PieEntry>()
            for (dataEntry in sortedList){
                val username = if (roomUsersList[dataEntry.key]!!.uid == firebaseUser.uid) "You" else roomUsersList[dataEntry.key]!!.username
                val entry = PieEntry(dataEntry.value, username)
                pieEntries.add(entry)
                i++
            }


            val dataSet = PieDataSet(pieEntries, "")
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS, 80)
            dataSet.valueTextColor = resources.getColor(R.color.colorGreen, resources.newTheme())
            dataSet.valueTextSize = 10f
            dataSet.setDrawValues(true)

            val pieData = PieData(dataSet)
            balanceChart.data = pieData
            balanceChart.isDrawHoleEnabled = false
            balanceChart.description.isEnabled = false
            balanceChart.animateY(1000)

            balanceChart.invalidate()
        }

        if (room.residents.size > 1 && usersExpenses.isNotEmpty()){

            getSettled(usersExpenses)

        }

    }

    private fun getSettled(userExpenses: HashMap<String, Float>){
        val topPayers = arrayListOf<String>()
        var maxAmount = 0f
        var myAmount = 0f
        if (userExpenses.containsKey(firebaseUser.uid)){
            myAmount = userExpenses[firebaseUser.uid]!!
        }
        // check who are the top payers
        // and what is the highest amount payed
        for (payer in userExpenses){
            val uid = payer.key
            val amount = payer.value

            if (maxAmount == 0f || amount > maxAmount){
                maxAmount = amount
                topPayers.clear()
                topPayers.add(uid)

            }
            else if (amount == maxAmount) {
                topPayers.add(uid)
            }
        }

        // check if someone owes me
        if (topPayers.contains(firebaseUser.uid)){
            breakEven.visibility = View.GONE
            val stringBuilder = StringBuilder()
            for (resident in roomUsersList.values){
                if (!topPayers.contains(resident.uid)){
                    val residentName = resident.username
                    val residentExpenses = if (userExpenses.containsKey(resident.uid)) userExpenses[resident.uid]!! else 0f
                    val payerDebt = ((totalAmount/room.residents.size)-(residentExpenses))/topPayers.size

                    val string = String.format(getString(R.string.owes_you), residentName, payerDebt, currencySymbol)
                    stringBuilder.append(string)
                }
            }

            debt.text = stringBuilder
        }
        // check who i owe to
        else {
            breakEven(userExpenses)

            val averageAmount = totalAmount/(room.residents.size)
            val myDebt = averageAmount - myAmount
            if (myDebt != 0f){
                if (topPayers.size == 1){
                    val topPayerName = roomUsersList[topPayers[0]]!!.username
                    debt.text = String.format(getString(R.string.your_owe), topPayerName, myDebt, currencySymbol)
                }
                else {
                    val debtSplit = myDebt/topPayers.size
                    val stringBuilder = StringBuilder()
                    for (topPayer in topPayers){
                        stringBuilder.append(String.format(getString(R.string.your_owe), topPayer, debtSplit, currencySymbol))
                    }
                    debt.text = stringBuilder
                }
            }

        }
    }

    private fun breakEven(userExpenses: HashMap<String, Float>){
        breakEven.visibility = View.VISIBLE
        databaseReference.child(Constants.break_even).child(room.uid).child(monthTimestamp.toString()).child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val value = snapshot.getValue<Boolean>()!!
                    if (!value){
                        breakEven.isClickable = true
                        breakEven.setTextColor(resources.getColor(R.color.colorPrimary, resources.newTheme()))
                        breakEven.text = getString(R.string.settle)
                    }
                    else {
                        breakEven.isClickable = false
                        breakEven.setTextColor(resources.getColor(R.color.colorGreen, resources.newTheme()))
                        breakEven.text = getString(R.string.settled)

                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


        breakEven.setOnClickListener {
            val dialog = GetCustomDialog(Dialog(myContext), R.layout.dialog_break_even).create()
            val approve = dialog.findViewById<Button>(R.id.approve)

            approve.setOnClickListener{
                databaseReference.child(Constants.break_even).child(room.uid).child(monthTimestamp.toString()).child(firebaseUser.uid).setValue(true).addOnSuccessListener {
                    getSettled(userExpenses)
                    dialog.dismiss()
                }
            }

            dialog.show()
        }




    }



}
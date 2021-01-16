package com.ezbalans.app.ezbalans.homeFragments

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.helpers.GetCurrentDate
import com.ezbalans.app.ezbalans.helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.rooms.roomFragments.FragmentDetails
import com.ezbalans.app.ezbalans.databinding.FragmentWalletBinding
import com.ezbalans.app.ezbalans.helpers.GetPrefs
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FragmentWallet : Fragment() {
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private val firebaseUser = Firebase.auth.currentUser!!
    private val databaseReference = Firebase.database.reference
    val payments = arrayListOf<Payment>()
    lateinit var myContext: Context

    var roomPayments = HashMap<String, HashMap<String, Payment>>()
    val myRooms = HashMap<String, Room>()
    var totalBudget = 0
    var roomBudgets = HashMap<String, Int>()
    var selectedFilter = GraphFilter.ThreeMonths



    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context

    }

    enum class GraphFilter {
        ThreeMonths, Year, AllTime
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.options.setOnClickListener {
            showOptions(it)
        }

        selectedFilter = GraphFilter.ThreeMonths
        binding.threeMonths.setTypeface(binding.threeMonths.typeface, Typeface.BOLD_ITALIC)

        binding.threeMonths.setOnClickListener {
            if (selectedFilter != GraphFilter.ThreeMonths) {
                selectedFilter = GraphFilter.ThreeMonths
                binding.threeMonths.setTypeface(binding.threeMonths.typeface, Typeface.BOLD_ITALIC)
                binding.year.setTypeface(null, Typeface.NORMAL)
                binding.allTime.setTypeface(null, Typeface.NORMAL)
                graphFilterPeriod()
            }
        }

        binding.year.setOnClickListener {
            if (selectedFilter != GraphFilter.Year) {
                selectedFilter = GraphFilter.Year
                binding.year.setTypeface(binding.threeMonths.typeface, Typeface.BOLD_ITALIC)
                binding.threeMonths.setTypeface(null, Typeface.NORMAL)
                binding.allTime.setTypeface(null, Typeface.NORMAL)
                graphFilterPeriod()
            }
        }

        binding.allTime.setOnClickListener {
            if (selectedFilter != GraphFilter.AllTime) {
                selectedFilter = GraphFilter.AllTime
                binding.allTime.setTypeface(binding.threeMonths.typeface, Typeface.BOLD_ITALIC)
                binding.threeMonths.setTypeface(null, Typeface.NORMAL)
                binding.year.setTypeface(null, Typeface.NORMAL)
                graphFilterPeriod()
            }
        }

        getMyBudgets()
    }


    private fun getMyBudgets() {
        val myBudgets = GetPrefs().getMyBudgets()
        for (entry in myBudgets){
            if(entry.key == firebaseUser.uid){
                totalBudget = entry.value.toInt()
            }
            else {
                roomBudgets[entry.key] = entry.value.toInt()
            }
        }
        getMyRooms()

//
//        databaseReference.child(Constants.budgets).child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (budget in snapshot.children) {
//                            if (budget.key == firebaseUser.uid) {
//                                totalBudget = budget.getValue<Int>()!!
//                            } else {
//                                roomBudgets[budget.key!!] = budget.getValue<Int>()!!
//                            }
//                        }
//                    }
//                    getMyRooms()
//                }
//
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//
//            })
    }

    private fun getMyRooms() {
        myRooms.clear()

        val myRoomsPref = GetPrefs().getMyRooms()
        for (room in myRoomsPref.values){
            if (room.residents[firebaseUser.uid] == Constants.added){
                myRooms[room.uid] = room
            }
        }
        getMyPayments()

//        databaseReference.child(Constants.rooms)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (rooms in snapshot.children) {
//                        val room = rooms.getValue<Room>()!!
//                        if (room.residents.containsKey(firebaseUser.uid)) {
//                            if (room.residents[firebaseUser.uid] == Constants.added) {
//                                myRooms[room.uid] = room
//                            }
//                        }
//                    }
//                    getMyPayments()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//
//            })
    }

    private fun getMyPayments() {
        payments.clear()
        roomPayments.clear()

        val myRoomsPref = GetPrefs().getMyRooms()
        val roomPaymentsPref = GetPrefs().getAllPayments()

        for (roomKey in myRoomsPref.keys){
            for (payment in roomPaymentsPref.values){
                if (payment.from == firebaseUser.uid){
                    val map = HashMap<String, Payment>()
                    map[payment.payment_uid] = payment
                    roomPayments[roomKey] = map
                    payments.add(payment)

                }

            }
        }
        graphFilterPeriod()



//        databaseReference.child(Constants.payments)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (room in snapshot.children) {
//                        if (myRooms.containsKey(room.key)) {
//                            val key = room.key!!
//                            for (payment in room.children) {
//                                val p = payment.getValue<Payment>()!!
//                                if (p.from == firebaseUser.uid) {
//                                    val hash = HashMap<String, Payment>()
//                                    hash[p.payment_uid] = p
//                                    roomPayments[key] = hash
//                                    payments.add(p)
//                                }
//                            }
//                        }
//                    }
//                    if (isAdded) {
//                        graphFilterPeriod()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//
//            })


    }

    private fun graphFilterPeriod() {
        val currentTime = GetCurrentDate().timestamp().toLong()
        val currentMonth = GetCurrentDate().monthNumber().toInt()
        val currentYear = GetCurrentDate().year().toInt()
        var pastTime: Long = 0

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when (selectedFilter) {
            GraphFilter.ThreeMonths -> {
                var pastMonth = 0;
                var pastYear = currentYear
                if (currentMonth < 3) {
                    pastMonth = 12 + currentMonth - 3
                    pastYear -= 1
                } else {
                    pastMonth = currentMonth - 3
                }

                calendar.set(Calendar.YEAR, pastYear)
                calendar.set(Calendar.MONTH, pastMonth - 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                pastTime = calendar.timeInMillis
            }

            GraphFilter.Year -> {
                calendar.set(Calendar.YEAR, currentYear - 1)
                calendar.set(Calendar.MONTH, currentMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                pastTime = calendar.timeInMillis
            }

            GraphFilter.AllTime -> {
                pastTime = 0

            }
        }

        showGraphs(pastTime, currentTime)
        // show graphs between periods

    }

    private fun showGraphs(pastTime: Long, currentTime: Long) {
        val filteredPayments = ArrayList<Payment>()
        for (payment in payments) {
            val timestamp = payment.timestamp.toLong()
            if (timestamp in pastTime..currentTime) {
                filteredPayments.add(payment)
            }
        }
        if (filteredPayments.isNotEmpty()) {
            binding.empty.visibility = View.GONE
            binding.categoryChartCard.visibility = View.VISIBLE
            binding.totalChartCard.visibility = View.VISIBLE
            binding.timeChartCard.visibility = View.VISIBLE

//            totalRoomsChart.visibility = View.VISIBLE
//            categoryChart.visibility = View.VISIBLE

            createCategoryChart(filteredPayments)
            createTotalRoomsChart(filteredPayments)
            createTimeInMonthChart(filteredPayments)
        } else {
            binding.empty.visibility = View.VISIBLE
            binding.categoryChartCard.visibility = View.GONE
            binding.totalChartCard.visibility = View.GONE
            binding.timeChartCard.visibility = View.GONE

        }

    }

    private fun createCategoryChart(payments: ArrayList<Payment>) {
        val paymentsByCategory = HashMap<String, Float>()

        for (payment in payments) {
            val category = payment.category
            when (paymentsByCategory.containsKey(category)) {
                true -> {
                    val currentValue = paymentsByCategory[category]!!
                    paymentsByCategory[category] = currentValue + payment.amount.toFloat()
                }
                false -> {
                    paymentsByCategory[category] = payment.amount.toFloat()
                }
            }
        }
        val sortedCategories = paymentsByCategory.toSortedMap()
        val categories = ArrayList(paymentsByCategory.keys)

        var i = 0
        val barEntries = arrayListOf<BarEntry>()
        for (dataEntry in sortedCategories) {
            val entry = BarEntry(i.toFloat(), dataEntry.value)
            barEntries.add(entry)
            i++
        }

        val dataSet = BarDataSet(barEntries, "")
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS, 80)
        dataSet.valueTextColor = resources.getColor(R.color.colorGreen, resources.newTheme())
        dataSet.valueTextSize = 10f
        dataSet.setDrawValues(true)

        val barData = BarData(dataSet)


        binding.categoryChart.axisRight.isEnabled = false
        binding.categoryChart.setNoDataText("No expenses to show yet")
        binding.categoryChart.setDrawBorders(true)
        binding.categoryChart.animateY(1000)
        binding.categoryChart.description.isEnabled = false
        binding.categoryChart.setScaleEnabled(true)
        binding.categoryChart.xAxis.labelCount = barEntries.size
        binding.categoryChart.xAxis.valueFormatter = FragmentDetails.MyXAxisFormatter(categories)
        binding.categoryChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.categoryChart.xAxis.setDrawGridLines(false)

        binding.categoryChart.data = barData
        binding.categoryChart.invalidate()

    }

    private fun createTotalRoomsChart(payments: ArrayList<Payment>) {
        val amountsByRoom = HashMap<String, Float>()
        for (payment in payments) {
            val room = payment.to
            if (amountsByRoom.containsKey(room)) {
                amountsByRoom[room] = (amountsByRoom[room]!! + payment.amount.toFloat())
            } else {
                amountsByRoom[room] = payment.amount.toFloat()
            }
        }

        // replace keys with names for the chart
        for (entry in amountsByRoom) {
            val roomKey = entry.key
            val roomValue = entry.value
            val roomName = myRooms[roomKey]!!.name
            amountsByRoom.remove(roomKey)
            amountsByRoom[roomName] = roomValue
        }

        val sortedCategories = amountsByRoom.toSortedMap()
        val categories = ArrayList(amountsByRoom.keys)

        var i = 0
        val barEntries = arrayListOf<BarEntry>()
        for (dataEntry in sortedCategories) {
            val entry = BarEntry(i.toFloat(), dataEntry.value)
            barEntries.add(entry)
            i++
        }

        val dataSet = BarDataSet(barEntries, "")
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS, 80)
        dataSet.valueTextColor = resources.getColor(R.color.colorGreen, resources.newTheme())
        dataSet.valueTextSize = 10f
        dataSet.setDrawValues(true)

        val barData = BarData(dataSet)


        binding.totalChart.axisRight.isEnabled = false
        binding.totalChart.setNoDataText("No expenses to show yet")
        binding.totalChart.setDrawBorders(true)
        binding.totalChart.animateY(1000)
        binding.totalChart.description.isEnabled = false
        binding.totalChart.setScaleEnabled(true)
        binding.totalChart.xAxis.labelCount = barEntries.size
        binding.totalChart.xAxis.valueFormatter = FragmentDetails.MyXAxisFormatter(categories)
        binding.totalChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.totalChart.xAxis.setDrawGridLines(false)

        binding.totalChart.data = barData
        binding.totalChart.invalidate()

    }

    private fun createTimeInMonthChart(payments: ArrayList<Payment>) {
        val paymentsByTime = HashMap<String, Float>()
        paymentsByTime["1"] = 0f
        paymentsByTime["2"] = 0f
        paymentsByTime["3"] = 0f

        for (payment in payments) {
            val timestamp = payment.timestamp.toLong()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            var period = ""

            when (day) {
                in 0 until 11 -> {
                    period = "1"
                }
                in 11 until 21 -> {
                    period = "2"
                }
                in 21..31 -> {
                    period = "3"
                }
            }
            paymentsByTime[period] = (paymentsByTime[period]!! + payment.amount.toFloat())

        }

        val sortedMap = paymentsByTime.toSortedMap()
        val categories = ArrayList<String>()
        categories.add(0, "1-10")
        categories.add(1, "11-20")
        categories.add(2, "21-31")

        var i = 0
        val barEntries = arrayListOf<BarEntry>()
        for (dataEntry in sortedMap) {
            val entry = BarEntry(i.toFloat(), dataEntry.value)
            barEntries.add(entry)
            i++
        }

        val dataSet = BarDataSet(barEntries, "")
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS, 80)
        dataSet.valueTextColor = resources.getColor(R.color.colorGreen, resources.newTheme())
        dataSet.valueTextSize = 10f
        dataSet.setDrawValues(true)

        val barData = BarData(dataSet)


        binding.timeChart.axisRight.isEnabled = false
        binding.timeChart.setNoDataText("No expenses to show yet")
        binding.timeChart.setDrawBorders(true)
        binding.timeChart.animateY(1000)
        binding.timeChart.description.isEnabled = false
        binding.timeChart.setScaleEnabled(true)
        binding.timeChart.xAxis.labelCount = barEntries.size
        binding.timeChart.xAxis.valueFormatter = FragmentDetails.MyXAxisFormatter(categories)
        binding.timeChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.timeChart.xAxis.setDrawGridLines(false)

        binding.timeChart.data = barData
        binding.timeChart.invalidate()

    }

    private fun editRoomBudget(roomUid: String) {
        val dialog = GetCustomDialog(Dialog(myContext), R.layout.dialog_edit_room_budget).create()
        val budget = dialog.findViewById<EditText>(R.id.budget)
        val apply = dialog.findViewById<Button>(R.id.apply)

        if (roomBudgets.containsKey(roomUid)) {
            budget.hint = roomBudgets[roomUid].toString()
        } else {
            budget.hint = "0"
        }

        apply.setOnClickListener {
            if (budget.text.isNotEmpty()) {
                val newBudget = budget.text.toString().toInt()
                databaseReference.child(Constants.budgets).child(firebaseUser.uid).child(roomUid)
                    .setValue(newBudget).addOnSuccessListener {
                    roomBudgets[roomUid] = newBudget
                    dialog.dismiss()
                    Toast.makeText(context, getString(R.string.room_budget_updated), Toast.LENGTH_SHORT).show()
                }
            } else {
                dialog.dismiss()
            }
        }


        dialog.show()

    }

    private fun showOptions(view: View) {
        val optionsPopup = PopupMenu(myContext, view)
        val inflater = optionsPopup.menuInflater
        inflater.inflate(R.menu.menu_budgets_options, optionsPopup.menu)

        optionsPopup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.total_budget -> {
                    showTotalBudgetDialog()
                }
                R.id.rooms_budget -> {
                    showRoomsDialog()
                }
            }
            true
        })
        optionsPopup.show()
    }

    private fun showTotalBudgetDialog() {
        val dialog = GetCustomDialog(Dialog(myContext), R.layout.dialog_edit_total_budget).create()
        val budget = dialog.findViewById<TextView>(R.id.budget)
        val apply = dialog.findViewById<Button>(R.id.apply)

        budget.hint = totalBudget.toString()
        apply.setOnClickListener {
            when {
                budget.text.isNotEmpty() && budget.text.toString() != totalBudget.toString() -> {
                    databaseReference.child(Constants.budgets).child(firebaseUser.uid)
                        .child(firebaseUser.uid).setValue(budget.text.toString().toInt())
                        .addOnSuccessListener {
                            dialog.dismiss()
                        }
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }
        dialog.show()

    }

    private fun showRoomsDialog() {

        val dialog = GetCustomDialog(Dialog(myContext), R.layout.dialog_rooms_list).create()
        val container = dialog.findViewById<LinearLayout>(R.id.container)

        for (r in myRooms) {
            val room = r.value
            var roomBudget: Int = 0
            var roomExpenses = 0
            if (roomBudgets.containsKey(room.uid)) {
                roomBudget = roomBudgets[room.uid]!!
            }

            for (payment in roomPayments[room.uid]!!) {
                val p = payment.value
                roomExpenses += p.amount.toInt()
            }

            val rowView = LayoutInflater.from(context).inflate(R.layout.row_room, container, false)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(10, 10, 10, 10)

            val image = rowView.findViewById<CircleImageView>(R.id.image)
            val name = rowView.findViewById<TextView>(R.id.name)!!
            val budget = rowView.findViewById<TextView>(R.id.identity_key)!!
            val actions = rowView.findViewById<ConstraintLayout>(R.id.actions)
            actions.visibility = View.GONE

            if (room.image.isNotEmpty()) {
                Picasso.get().load(room.image).into(image)
            } else {
                Picasso.get().load(R.drawable.default_account).into(image)
            }
            name.text = room.name
            budget.text = roomBudget.toString()

            rowView.setOnClickListener {
                dialog.dismiss()
                editRoomBudget(room.uid)
            }
            container.addView(rowView)
        }
        dialog.show()
    }


}
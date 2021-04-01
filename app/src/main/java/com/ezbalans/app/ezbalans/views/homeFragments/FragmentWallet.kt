package com.ezbalans.app.ezbalans.views.homeFragments

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ezbalans.app.ezbalans.utils.Constants
import com.ezbalans.app.ezbalans.utils.GetCurrentDate
import com.ezbalans.app.ezbalans.utils.GetCustomDialog
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.views.rooms.roomFragments.FragmentDetails
import com.ezbalans.app.ezbalans.databinding.FragmentWalletBinding
import com.ezbalans.app.ezbalans.viewmodels.homeFragments.WalletFragmentViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class FragmentWallet : Fragment() {
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private val firebaseUser = Firebase.auth.currentUser!!
    private val databaseReference = Firebase.database.reference
    private var payments = arrayListOf<Payment>()
    private var roomPayments = HashMap<String, HashMap<String, Payment>>()
    private val myRooms = HashMap<String, Room>()

    private var totalBudget = 0
    private var roomBudgets = HashMap<String, Int>()
    private var selectedFilter = GraphFilter.ThreeMonths

    private val viewModel: WalletFragmentViewModel by viewModels()


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class GraphFilter {
        ThreeMonths, Year, AllTime
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        initViewModels()
    }

    private fun initViewModels(){
        viewModel.getMyBudgets().observe(viewLifecycleOwner, { budgets ->
            for (entry in budgets) {
                if (entry.key == firebaseUser.uid) {
                    totalBudget = entry.value
                } else {
                    roomBudgets[entry.key] = entry.value
                }
            }

        })

        viewModel.getMyRooms().observe(viewLifecycleOwner, { result ->
            for (room in result){
                myRooms[room.uid] = room
            }

        })

        viewModel.getMyPayments().observe(viewLifecycleOwner, {
            payments.clear()
            roomPayments.clear()

            payments.addAll(it.values)

            for (payment in it.values){
                if (roomPayments.containsKey(payment.to)){
                    roomPayments[payment.to]!![payment.payment_uid] = payment
                }
                else {
                    val paymentHash = hashMapOf<String, Payment>()
                    paymentHash[payment.payment_uid] = payment
                    roomPayments[payment.to] = paymentHash
                }
            }
            graphFilterPeriod()
        })
    }


    private fun graphFilterPeriod() {
        val currentTime = GetCurrentDate().timestamp().toLong()
        val currentMonth = GetCurrentDate().monthNumber().toInt()
        val currentYear = GetCurrentDate().year().toInt()
        var pastTime: Long = 0

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when (selectedFilter) {
            GraphFilter.ThreeMonths -> {
                val pastMonth: Int
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
        val paymentsByCategory = TreeMap<String, Float>()

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

        val categories = ArrayList(paymentsByCategory.keys)

        var i = 0
        val barEntries = arrayListOf<BarEntry>()
        for (dataEntry in paymentsByCategory) {
            val entry = BarEntry(i.toFloat(), dataEntry.value)
            barEntries.add(entry)
            i++
        }

        val dataSet = BarDataSet(barEntries, "")
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS, 80)
        if (isAttached()){
            dataSet.valueTextColor = resources.getColor(R.color.colorGreen, resources.newTheme())
        }
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

        val amountsByRoomName = HashMap<String, Float>()

        // replace keys with names for the chart
        for (entry in amountsByRoom) {
            val roomKey = entry.key
            val roomValue = entry.value
            val roomName = myRooms[roomKey]!!.name
            amountsByRoomName[roomName] = roomValue
        }

        val sortedCategories = amountsByRoomName.toSortedMap()
        val categories = ArrayList(amountsByRoomName.keys)

        var i = 0
        val barEntries = arrayListOf<BarEntry>()
        for (dataEntry in sortedCategories) {
            val entry = BarEntry(i.toFloat(), dataEntry.value)
            barEntries.add(entry)
            i++
        }

        val dataSet = BarDataSet(barEntries, "")
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS, 80)
        if (isAttached()){
            dataSet.valueTextColor = resources.getColor(R.color.colorGreen, resources.newTheme())

        }
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
            val calendar = Calendar.getInstance(TimeZone.getDefault())
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
        if (isAttached()){
            dataSet.valueTextColor = resources.getColor(R.color.colorGreen, resources.newTheme())
        }
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

    private fun editRoomBudgetDialog(roomUid: String) {
        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_edit_room_budget).create()
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
        val optionsPopup = PopupMenu(requireContext(), view)
        val inflater = optionsPopup.menuInflater
        inflater.inflate(R.menu.menu_budgets_options, optionsPopup.menu)

        optionsPopup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.total_budget -> {
                    showTotalBudgetDialog()
                }
                R.id.rooms_budget -> {
                    showRoomsDialog()
                }
            }
            true
        }
        optionsPopup.show()
    }

    private fun showTotalBudgetDialog() {
        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_edit_total_budget).create()
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
        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_rooms_list).create()
        val container = dialog.findViewById<LinearLayout>(R.id.container)

        for (r in myRooms) {
            val room = r.value
            var roomBudget = 0
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
                editRoomBudgetDialog(room.uid)
            }
            container.addView(rowView)
        }
        dialog.show()
    }

    private fun isAttached() : Boolean {
        return (isVisible && activity != null)
    }


}
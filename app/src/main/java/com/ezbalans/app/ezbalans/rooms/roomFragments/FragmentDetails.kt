package com.ezbalans.app.ezbalans.rooms.roomFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.databinding.FragmentRoomDetailsBinding
import com.ezbalans.app.ezbalans.eventBus.PaymentsEvent
import com.ezbalans.app.ezbalans.helpers.GetPrefs
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.lang.StringBuilder
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class FragmentDetails: Fragment(){
    private var _binding: FragmentRoomDetailsBinding? = null
    private val binding get() = _binding!!

    private val firebaseUser = Firebase.auth.currentUser!!
    private val databaseReference = Firebase.database.reference

    val payments = arrayListOf<Payment>()
    val users = arrayListOf<User>()
    val usersList = java.util.HashMap<String, User>()

    private lateinit var totalChart: LineChart
    private lateinit var categoryChart: BarChart
    private lateinit var balanceChart: PieChart
    private lateinit var debt: TextView

    private lateinit var totalChartCard: CardView
    private lateinit var categoryChartCard: CardView
    private lateinit var balanceChartCard: CardView


    var currencySymbol = ""
    var room = Room()
    var totalAmount = 0

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRoomDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roomUid = arguments?.getString(Constants.room_uid)!!
        totalChart = binding.totalChart
        categoryChart = binding.categoryChart
        balanceChart = binding.balancePieChart

        totalChartCard = binding.totalCard
        categoryChartCard = binding.categoryCard
        balanceChartCard = binding.balanceCard

        debt = binding.debt

        loadRoom(roomUid)
    }

    private fun loadRoom(roomUid: String){
        val roomsPref = GetPrefs().getAllRooms()
        room = roomsPref[roomUid]!!
        currencySymbol = if (room.currency == Constants.nis) Constants.nis_symbol else Constants.usd_symbol
        loadRoomUsers()

    }

    private fun loadRoomUsers(){
        users.clear()

        val usersPref = GetPrefs().getAllUsers()
        for (resident in room.residents.keys){
            val user = usersPref[resident]!!
            users.add(user)
            usersList[user.uid]= user
        }
        loadPayments()
    }

    @Subscribe
    fun onPaymentsUpdate(event: PaymentsEvent){
        loadPayments()
    }

    private fun loadPayments(){
        payments.clear()
        totalAmount = 0

        val paymentsPref = GetPrefs().getAllPayments()
        for (payment in paymentsPref.values){
            if (payment.to == room.uid && payment.status == Constants.payment_valid && paymentFromThisMonth(payment)){
                payments.add(payment)
                totalAmount += payment.amount.toInt()

            }
        }

        if (payments.isNotEmpty()){
            payments.sortWith { obj1, obj2 -> obj1.timestamp.compareTo(obj2.timestamp) }
            createTotalExpensesChart()
            createCategoryChart()
            createBalanceChart()
        }
        else {
            binding.emptyItem.visibility = View.VISIBLE
        }
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

    private fun createTotalExpensesChart(){
        val paymentsData = HashMap<Float, Float>()
        var totalAmount = 0f
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH).toFloat()

        binding.totalTitle.text = (binding.totalTitle.text.toString() + " ($currencySymbol)")

        for (payment in payments){
            calendar.timeInMillis = payment.timestamp.toLong()
            val day = calendar.get(Calendar.DAY_OF_MONTH).toFloat()
            val amount = payment.amount.toFloat()

            if (!paymentsData.containsKey(day)){
                paymentsData[day] = 0f

            }
            val currentDayAmount = paymentsData[day]!!

            paymentsData[day] = currentDayAmount + amount
        }

        val entries = arrayListOf<Entry>()
        val sorted = paymentsData.toSortedMap()

        if (sorted.isNotEmpty()){
            totalChartCard.visibility = View.VISIBLE

            for (key in sorted.keys){
                totalAmount += paymentsData[key]!!
                val entry = Entry(key, totalAmount)
                entries.add(entry)
            }
            val dataSet = LineDataSet(entries, "")
            dataSet.color = resources.getColor(R.color.colorGreen, resources.newTheme())
            dataSet.fillColor = resources.getColor(R.color.colorGreen, resources.newTheme())
            dataSet.valueTextColor = resources.getColor(R.color.colorGreen, resources.newTheme())
            dataSet.lineWidth = 3f
            dataSet.valueTextSize = 10f
            dataSet.fillAlpha = 50
            dataSet.setDrawValues(true)
            dataSet.setDrawCircles(false)
            dataSet.setDrawFilled(true)
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER


            val lineData = LineData(dataSet)

            totalChart.axisRight.isEnabled = false
            if (room.monthly_budget != "0"){
                totalChart.axisLeft.axisMaximum = room.monthly_budget.toFloat()
            }
            totalChart.xAxis.axisMaximum = maxDays
            totalChart.axisLeft.setDrawZeroLine(true)
            totalChart.axisLeft.zeroLineColor = resources.getColor(R.color.colorGreen, resources.newTheme())

            totalChart.description.isEnabled = false
            totalChart.setScaleEnabled(true)
            totalChart.setNoDataText("No expenses to show yet")
            totalChart.setDrawBorders(true)
            totalChart.animateY(1000)

            totalChart.data = lineData
            totalChart.invalidate()
        }

    }

    private fun createCategoryChart(){
        val categories = arrayListOf<String>()
        val paymentData = HashMap<String, Float>()
        binding.categoryTitle.text = (binding.categoryTitle.text.toString() + " ($currencySymbol)")

        // collect payments per type
        for (payment in payments){
            val category = payment.category
            when (categories.contains(category)){
                true -> {
                    val currentValue = paymentData[category]!!
                    paymentData[category] = currentValue + payment.amount.toFloat()
                }
                false -> {
                    categories.add(categories.size, category)
                    paymentData[category] = payment.amount.toFloat()
                }
            }
        }

        // transform data
        categories.sort()

        val sorted = paymentData.toSortedMap()

        if (sorted.isNotEmpty()){
            categoryChartCard.visibility = View.VISIBLE

            var i = 0
            val barEntries = arrayListOf<BarEntry>()
            for (dataEntry in sorted){
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


            categoryChart.axisRight.isEnabled = false
            categoryChart.setNoDataText("No expenses to show yet")
            categoryChart.setDrawBorders(true)
            categoryChart.animateY(1000)
            categoryChart.description.isEnabled = false
            categoryChart.setScaleEnabled(true)
            categoryChart.xAxis.labelCount = barEntries.size
            categoryChart.xAxis.valueFormatter = MyXAxisFormatter(categories)
            categoryChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            categoryChart.xAxis.setDrawGridLines(false)


            categoryChart.data = barData

            categoryChart.invalidate()
        }
    }

    private fun createBalanceChart(){
        val users = arrayListOf<String>()
        val usersExpenses = HashMap<String, Float>()

        binding.balanceTitle.text = (binding.balanceTitle.text.toString() + " ($currencySymbol)")


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
                val username = if (usersList[dataEntry.key]!!.uid == firebaseUser.uid) "You" else usersList[dataEntry.key]!!.username
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
            Log.d("TAG", "createBalanceChart: get settled")

            getSettled(usersExpenses)

        }

    }

    private fun getSettled(userExpenses: HashMap<String, Float>){
        val currencySymbol = if (room.currency == Constants.nis) Constants.nis_symbol else Constants.usd_symbol

        val topPayers = arrayListOf<String>()
        var myAmount = 0f
        if (userExpenses.containsKey(firebaseUser.uid)){
            myAmount = userExpenses[firebaseUser.uid]!!
        }

        // calculate number of payers
        var numberOfPayers = 0
        for (resident in room.residents){
            if (resident.value == Constants.added){
                numberOfPayers +=1
            }
        }


        // check who are the top payers
        // who paid more than the total equal split
        val splitAmount = (totalAmount/numberOfPayers).toFloat()
        for (payer in userExpenses){
            val uid = payer.key
            val amount = payer.value
            if (amount > splitAmount){
                topPayers.add(uid)
            }
        }

        // check if someone owes me
        if (topPayers.contains(firebaseUser.uid)){
                val stringBuilder = StringBuilder()

                var totalExtra = 0f
                val myExtra = userExpenses[firebaseUser.uid]!! - splitAmount
                totalExtra += myExtra
                for (topPayer in topPayers){
                    if (topPayer != firebaseUser.uid){
                         totalExtra += userExpenses[topPayer]!! - splitAmount
                    }
                }
                val myPercentage = (myExtra/totalExtra).toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
                for (payer in userExpenses.keys){
                    if (userExpenses[payer]!! < splitAmount){
                        val payerName = usersList[payer]!!.username
                        val payerDebt = (splitAmount - userExpenses[payer]!!).toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
                        val payerDebtRelative = (payerDebt * myPercentage).setScale(0, RoundingMode.HALF_EVEN)
                        Log.d("TAG", "getSettled: $payerName // $payerDebt // $payerDebtRelative //$splitAmount")

                        val string = String.format(getString(R.string.owes_you), payerName, payerDebtRelative, currencySymbol+"\n")
                        stringBuilder.append(string)

                    }

                }
                debt.text = stringBuilder

        }
        // check who i owe to
        else {
            val myDebt = (splitAmount - myAmount).toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
            if (topPayers.size == 1){
                val topPayerName = usersList[topPayers[0]]!!.username
                debt.text = String.format(getString(R.string.your_owe), topPayerName, myDebt, currencySymbol)
            }
            else {
                var totalExtra = 0f
                val topPayersExtra = hashMapOf<String, Float>()
                val stringBuilder = StringBuilder()

                for (topPayer in topPayers){
                    val extraAmount = (userExpenses[topPayer]!!-splitAmount)
                    totalExtra += extraAmount
                    topPayersExtra[topPayer] = extraAmount
                }

                for (topPayer in topPayersExtra.keys){
                    val payerName = usersList[topPayer]!!.username
                    val percentage = ((topPayersExtra[topPayer]!!) / totalExtra).toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
                    val myDebtToPayer = (myDebt * percentage).setScale(0, RoundingMode.HALF_EVEN)

                    stringBuilder.append(String.format(getString(R.string.your_owe), payerName, myDebtToPayer, currencySymbol+"\n"))

                    Log.d("TAG", "getSettled: $payerName // $percentage //")

                }
                debt.text = stringBuilder
            }
        }
    }

    class MyXAxisFormatter(categories: ArrayList<String>) : ValueFormatter() {
        private val array: Array<String> = categories.toTypedArray()

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return array.getOrNull(value.toInt()) ?: value.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }



}
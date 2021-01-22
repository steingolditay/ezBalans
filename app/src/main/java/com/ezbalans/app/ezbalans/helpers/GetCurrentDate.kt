package com.ezbalans.app.ezbalans.helpers

import java.text.SimpleDateFormat
import java.util.*

class GetCurrentDate {
    private val date = Date()

    fun formatted(): String{
        val formatter = SimpleDateFormat("dd//MM/yyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)
    }

    fun timestamp(): String{
        val calendar = Calendar.getInstance(TimeZone.getDefault()).timeInMillis
        return calendar.toString()
    }

    fun monthName(): String{
        val formatter = SimpleDateFormat("MMMM", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)
    }

    fun monthNumber(): String{
        val formatter = SimpleDateFormat("MM", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)
    }

    fun year(): String{
        val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)
    }

    fun customMonthName(timestamp: Long): String{
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMMM", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)

    }


    fun customYear(timestamp: Long): String{

        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)

    }

    fun dateFromTimestamp(timestamp: Long): String{

        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd/MM//yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)

    }

    fun dateAndTimeFromTimestamp(timestamp: Long): String{
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = timestamp
        val formatter = SimpleDateFormat("dd/MM//yyyy hh:mm:ss", Locale.getDefault())
        return formatter.format(calendar.timeInMillis)

    }

}


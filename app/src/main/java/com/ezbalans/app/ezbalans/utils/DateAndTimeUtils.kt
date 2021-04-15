package com.ezbalans.app.ezbalans.utils

import java.text.SimpleDateFormat
import java.util.*

object DateAndTimeUtils {
    private val calendar = Calendar.getInstance(TimeZone.getDefault())

    fun formattedCurrentDateString(): String{
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(Date())
    }

    fun currentTimestamp(): String{
        val calendar = Calendar.getInstance(TimeZone.getDefault()).timeInMillis
        return calendar.toString()
    }

    fun currentMonthName(): String{
        val formatter = SimpleDateFormat("MMMM", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(Date())
    }

    fun currentMonthNumber(): String{
        val formatter = SimpleDateFormat("MM", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(Date())
    }

    fun currentYear(): String{
        val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(Date())
    }

    fun monthNameFromCustomTimestamp(timestamp: Long): String{
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMMM", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)

    }


    fun yearFromCustomTimestamp(timestamp: Long): String{
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)

    }

    fun dateFromCustomTimestamp(timestamp: Long): String{
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd/MM//yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        return formatter.format(date)

    }

    fun dateAndTimeFromTimestamp(timestamp: Long): String{
        calendar.clear()
        calendar.timeInMillis = timestamp
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())
        return formatter.format(calendar.timeInMillis)
    }

    fun flatTimestampForDayInThisMonth(day: Int): String{
        calendar.clear()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return (calendar.time.time).toString()
    }

    fun currentMonthMaximumDays() : Int{
        calendar.clear()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

}


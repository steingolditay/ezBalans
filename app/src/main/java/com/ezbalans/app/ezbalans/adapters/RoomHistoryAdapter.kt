package com.ezbalans.app.ezbalans.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ezbalans.app.ezbalans.utils.Constants
import com.ezbalans.app.ezbalans.R
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RoomHistoryAdapter(private val pastPayments: SortedMap<Long, Int>,
                         private val keys: ArrayList<Long>,
                         private val currency: String,
                         private val listener: OnItemClickListener) :
        RecyclerView.Adapter<RoomHistoryAdapter.ViewHolder>() {

    val databaseReference = Firebase.database.reference



    interface OnItemClickListener{
        fun onItemClick(position: Long)
    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val year: TextView = itemView.findViewById(R.id.year)
        val month: TextView = itemView.findViewById(R.id.month)
        val total: TextView = itemView.findViewById(R.id.total)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(keys[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomHistoryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_room_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currencySymbol = if (currency == Constants.nis) Constants.nis_symbol else Constants.usd_symbol

        val timestamp = keys[position]
        val totalAmount = pastPayments[timestamp]

        val year = getYear(timestamp)
        val month = getMonth(timestamp)

        holder.month.text = month
        holder.year.text = year
        holder.total.text = totalAmount.toString() + " $currencySymbol"




    }

    override fun getItemCount(): Int {
        return pastPayments.size
    }

    private fun getYear(timestamp: Long): String{
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    private fun getMonth(timestamp: Long): String{
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMMM", Locale.getDefault())
        return formatter.format(date)
    }


}
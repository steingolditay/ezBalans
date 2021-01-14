package com.ezbalans.app.ezbalans.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.Models.Payment
import com.ezbalans.app.ezbalans.Models.User
import com.ezbalans.app.ezbalans.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList


class RoomPaymentsAdapter (private val context: Context,
                           private val payments: ArrayList<Payment>,
                           private val users: ArrayList<User>,
                           private val currency: String,
                           private val listener: OnItemClickListener) :
        RecyclerView.Adapter<RoomPaymentsAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val image: CircleImageView = itemView.findViewById(R.id.image)
        val name: TextView = itemView.findViewById(R.id.name)
        val category: TextView = itemView.findViewById(R.id.category)
        val date: TextView = itemView.findViewById(R.id.date)
        val amount: TextView = itemView.findViewById(R.id.amount)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_payment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payment = payments[position]
        val user = getUser(payment.from)
        val currencySymbol = if (currency == Constants.nis) Constants.nis_symbol else Constants.usd_symbol


        if (user.image.isNotEmpty()){
            Picasso.get().load(user.image).into(holder.image)
        }
        holder.name.text = user.username
        holder.category.text = payment.category
        holder.date.text = getPaymentDate(payment.timestamp)
        holder.amount.text = payment.amount + "$currencySymbol"
    }

    override fun getItemCount(): Int {
        return payments.size
    }

    private fun getUser(uid: String): User{
        var user = User()
        for (u in users){
            if (u.uid == uid){
                user = u
                return user
            }
        }
        return user
    }

    private fun getPaymentDate(timestamp: String): String{
        val timeStamp = timestamp.toLong()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp

        return (calendar.get(Calendar.DAY_OF_MONTH)).toString()
    }


}


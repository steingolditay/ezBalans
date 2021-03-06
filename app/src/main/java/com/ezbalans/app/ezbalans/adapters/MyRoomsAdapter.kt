package com.ezbalans.app.ezbalans.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.helpers.GetPrefs
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

class MyRoomsAdapter(private val context: Context, private val rooms: ArrayList<Room>, private val listener: MyRoomsAdapter.OnItemClickListener) :
        RecyclerView.Adapter<MyRoomsAdapter.ViewHolder>() {

    private val firebaseUser = Firebase.auth.currentUser!!
    private val databaseReference = Firebase.database.reference


    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onCartClick(position: Int)
        fun onDetailsClick(position: Int)
        fun onEditClick(position: Int)
    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val image: CircleImageView = itemView.findViewById(R.id.image)
        val name: TextView = itemView.findViewById(R.id.name)
        val identityKey: TextView = itemView.findViewById(R.id.identity_key)
        private val cart: ImageView = itemView.findViewById(R.id.cart)
        private val details: ImageView = itemView.findViewById(R.id.details)
        private val edit: ImageView = itemView.findViewById(R.id.edit)
        val motd_title: TextView = itemView.findViewById(R.id.motd_title)
        val motd: TextView = itemView.findViewById(R.id.motd)
        val budget:TextView = itemView.findViewById(R.id.budget)


        init {

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    listener.onItemClick(position)
                }
            }

            cart.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    listener.onCartClick(position)
                }
            }

            details.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    listener.onDetailsClick(position)
                }
            }

            edit.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    listener.onEditClick(position)
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRoomsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyRoomsAdapter.ViewHolder, position: Int) {
        val room = rooms[position]

        holder.name.text = room.name
        holder.identityKey.text = room.identity_key

        if (room.motd.isEmpty()){
            holder.motd.visibility = View.GONE
            holder.motd_title.visibility = View.GONE
        }
        holder.motd.text = room.motd

        Picasso.get().load(room.image).into(holder.image)
        loadBudget(room.uid, holder, room.currency)

    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    private fun loadBudget(roomUid: String, holder: MyRoomsAdapter.ViewHolder, roomCurrency: String){
        var totalAmount = 0
        var budget = 0
        val currency = if (roomCurrency == Constants.nis) Constants.nis_symbol else Constants.usd_symbol

        val myBudgets = GetPrefs().getMyBudgets()
        if (myBudgets.containsKey(roomUid)){
            budget = myBudgets[roomUid]!!.toInt()
        }
        else {
            databaseReference.child(Constants.budgets).child(firebaseUser.uid).child(roomUid).setValue(0)
        }

        val paymentsPref = GetPrefs().getMyPayments()
        for (payment in paymentsPref.values){
            if (payment.status == Constants.payment_valid && paymentFromThisMonth(payment) && payment.to == roomUid){
                totalAmount += payment.amount.toInt()
            }
        }

        holder.budget.text = "$totalAmount/$budget$currency"

        when {
            budget == 0 || totalAmount < budget -> {
                holder.budget.setTextColor(context.resources.getColor(R.color.colorGreen, context.resources.newTheme()))
            }
            else -> {
                holder.budget.setTextColor(context.resources.getColor(R.color.colorPrimary, context.resources.newTheme()))
            }
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

}
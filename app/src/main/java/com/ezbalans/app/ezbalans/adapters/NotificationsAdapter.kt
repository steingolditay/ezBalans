package com.ezbalans.app.ezbalans.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ezbalans.app.ezbalans.utils.Constants
import com.ezbalans.app.ezbalans.utils.DateAndTimeUtils
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.HashMap

class NotificationsAdapter(private val context: Context,
                           private val notifications: ArrayList<Notification>,
                           private val users: HashMap<String, User>,
                           private val rooms: HashMap<String, Room>,
                           private val listener: OnItemClickListener) :
        RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    val firebaseUser = Firebase.auth.currentUser!!

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val image: CircleImageView = itemView.findViewById(R.id.image)
        val title: TextView = itemView.findViewById(R.id.title)
        val message: TextView = itemView.findViewById(R.id.message)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        val room = rooms[notification.room_uid]!!

        if (notification.seen){
            holder.itemView.alpha = 0.5f
        }

        holder.timestamp.text = DateAndTimeUtils().dateAndTimeFromTimestamp(notification.timestamp)

        when (notification.type){
            Constants.notify_user_joined -> {
                if (notification.target_uid == firebaseUser.uid){

                    Picasso.get().load(room.image).into(holder.image)
                    holder.title.text = room.name
                    holder.message.text = context.getString(R.string.you_joined_room)
                }
                else{
                    val user = users[notification.target_uid]!!
                    Picasso.get().load(user.image).into(holder.image)
                    holder.title.text = room.name
                    holder.message.text = String.format(context.getString(R.string.joined_the_room), user.username)
                }
            }

            Constants.notify_user_quit -> {
                val user = users[notification.source_uid]!!
                Picasso.get().load(user.image).into(holder.image)
                holder.title.text = room.name
                holder.message.text = String.format(context.getString(R.string.left_the_room), user.username)
            }

            Constants.notify_user_removed -> {
                if (notification.target_uid == firebaseUser.uid){
                    Picasso.get().load(room.image).into(holder.image)
                    holder.title.text = room.name
                    holder.message.text = context.getString(R.string.you_removed_from_room)
                }
                else{
                    val user = users[notification.target_uid]!!
                    val admin = users[notification.source_uid]!!
                    Picasso.get().load(user.image).into(holder.image)
                    holder.title.text = room.name
                    holder.message.text = String.format(context.getString(R.string.removed_from_room), user.username, admin.username)
                }
            }

            Constants.notify_user_requested -> {
                val user = users[notification.source_uid]!!
                Picasso.get().load(user.image).into(holder.image)
                holder.title.text = room.name
                holder.message.text = String.format(context.getString(R.string.request_to_join_room), user.username)
            }

            Constants.notify_room_info_changed -> {
                val admin = users[notification.source_uid]!!

                Picasso.get().load(room.image).into(holder.image)
                holder.title.text = room.name
                holder.message.text = String.format(context.getString(R.string.room_info_changed), admin.username)
            }

            Constants.notify_payment_invalid -> {
                val user = users[notification.source_uid]!!
                Picasso.get().load(user.image).into(holder.image)
                holder.title.text = room.name
                holder.message.text = String.format(context.getString(R.string.invalid_payment_submitted), user.username)
            }

            Constants.notify_payment_validated -> {
                val admin = users[notification.source_uid]!!
                val user = users[notification.target_uid]!!
                if (user.uid == firebaseUser.uid){
                    Picasso.get().load(room.image).into(holder.image)
                    holder.title.text = room.name
                    holder.message.text = String.format(context.getString(R.string.invalid_payment_approved), admin.username)
                }
                else {
                    Picasso.get().load(room.image).into(holder.image)
                    holder.title.text = room.name
                    holder.message.text = String.format(context.getString(R.string.invalid_payment_approved), admin.username, user.username)
                }
            }

            Constants.notify_payment_declined -> {
                val admin = users[notification.source_uid]!!
                val user = users[notification.target_uid]!!
                if (user.uid == firebaseUser.uid){
                    Picasso.get().load(admin.image).into(holder.image)
                    holder.title.text = room.name
                    holder.message.text = String.format(context.getString(R.string.invalid_payment_declined), admin.username)
                }
                else {
                    Picasso.get().load(room.image).into(holder.image)
                    holder.title.text = room.name
                    holder.message.text = String.format(context.getString(R.string.invalid_payment_declined), admin.username, user.username)
                }
            }

            Constants.notify_room_closed -> {
                val admin = users[notification.source_uid]!!
                Picasso.get().load(room.image).into(holder.image)
                holder.title.text = room.name
                holder.message.text = String.format(context.getString(R.string.closed_the_room), admin.username)
            }

            Constants.notify_admin_demoted -> {
                val user = users[notification.target_uid]!!
                Picasso.get().load(room.image).into(holder.image)
                holder.title.text = room.name
                holder.message.text = String.format(context.getString(R.string.no_longer_admin), user.username)
            }

            Constants.notify_admin_promoted -> {
                val user = users[notification.target_uid]!!
                Picasso.get().load(room.image).into(holder.image)
                holder.title.text = room.name
                holder.message.text = String.format(context.getString(R.string.is_admin), user.username)
            }

            Constants.notify_motd_changed -> {
                val user = users[notification.source_uid]!!
                Picasso.get().load(room.image).into(holder.image)
                holder.title.text = room.name
                holder.message.text = String.format(context.getString(R.string.new_motd), user.username)
            }

        }

    }

    override fun getItemCount(): Int {
        return notifications.size
    }


}
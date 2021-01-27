package com.ezbalans.app.ezbalans

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.adapters.NotificationsAdapter
import com.ezbalans.app.ezbalans.databinding.ViewMainframeBinding
import com.ezbalans.app.ezbalans.databinding.ViewNotificationsBinding
import com.ezbalans.app.ezbalans.eventBus.NotificationsEvent
import com.ezbalans.app.ezbalans.helpers.CreateNotification
import com.ezbalans.app.ezbalans.helpers.GetCurrentDate
import com.ezbalans.app.ezbalans.helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.helpers.GetPrefs
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class Notifications: AppCompatActivity(), NotificationsAdapter.OnItemClickListener {
    private lateinit var binding: ViewNotificationsBinding

    private val databaseReference = Firebase.database.reference
    private val firebaseUser = Firebase.auth.currentUser!!
    private val rooms = hashMapOf<String, Room>()
    private var users =  HashMap<String, User>()
    private val notifications =  ArrayList<Notification>()

    lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewNotificationsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        getPrefs()

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

    }

    @Subscribe
    fun onNotificationsUpdate(event: NotificationsEvent) {
        getPrefs()
    }


    private fun getPrefs() {
        users.clear()
        notifications.clear()
        rooms.clear()


        val getRooms = GetPrefs().getAllRooms()
        for (room in getRooms.values){
            if (room.status == Constants.room_active)
                rooms[room.uid] = room
        }

        val getNotifications = GetPrefs().getNotifications()
        for (notification in getNotifications.values){
            notifications.add(notification)
        }
        if (notifications.isEmpty()) {
            binding.list.visibility = View.GONE
            binding.emptyItem.visibility = View.VISIBLE
        } else {
            notifications.sortWith { obj1, obj2 -> obj1.timestamp.compareTo(obj2.timestamp) }
            notifications.reverse()
            loadNotifications()
        }
    }

    private fun loadNotifications() {
        adapter = NotificationsAdapter(this, notifications, users, rooms, this)
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(this)
    }

    override fun onItemClick(position: Int) {
        val notification = notifications[position]
        databaseReference.child(Constants.notifications).child(firebaseUser.uid).child(notification.uid).child(Constants.seen).setValue(true).addOnSuccessListener {
            notification.seen = true
            adapter.notifyDataSetChanged()
        }

        when (notification.type) {
            Constants.notify_user_joined -> {
                showUserJoinedDialog(notification)
            }

            Constants.notify_user_quit -> {
                showUserQuitDialog(notification)
            }

            Constants.notify_user_removed -> {
                showUserRemovedDialog(notification)
            }

            Constants.notify_user_requested -> {
                showUserRequestedDialog(notification)
            }

            Constants.notify_room_info_changed -> {
                showRoomInfoChangedDialog(notification)

            }

            Constants.notify_payment_invalid -> {
                showPaymentInvalidDialog(notification)

            }

            Constants.notify_payment_validated -> {
                showPaymentValidatedDialog(notification)

            }

            Constants.notify_payment_declined -> {
                showPaymentDeclinedDialog(notification)

            }

            Constants.notify_room_closed -> {
                showRoomClosedDialog(notification)

            }

            Constants.notify_admin_demoted -> {
                showAdminDemotedDialog(notification)

            }

            Constants.notify_admin_promoted -> {
                showAdminPromotedDialog(notification)

            }
            Constants.notify_motd_changed -> {
                showMOTDChangedDialog(notification)

            }
        }
    }

    private fun showUserJoinedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username
        val room = rooms[notification.room_uid]!!.name


        if (notification.target_uid == firebaseUser.uid) {
            title.text = getString(R.string.welcome)
            body.text = String.format(getString(R.string.youre_added_to_room), admin, room)

        }

        else {
            title.text = getString(R.string.new_resident)
            body.text = String.format(getString(R.string.added_to_room), admin, user)
        }
        dialog.show()
    }

    private fun showUserQuitDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val user = users[notification.source_uid]!!.username

        title.text = getString(R.string.resident_left)
        body.text = String.format(getString(R.string.resident_left_body), user)


        dialog.show()
    }

    private fun showUserRemovedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username
        val room = rooms[notification.room_uid]!!.name

        title.text = getString(R.string.resident_removed)
        if (notification.target_uid == firebaseUser.uid) {
            body.text = String.format(getString(R.string.resident_removed_body), admin, room)
        }
        else {
            body.text = String.format(getString(R.string.resident_removed_body_2), admin, user, user)
        }

        dialog.show()
    }

    private fun showRoomInfoChangedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username

        title.text = getString(R.string.room_info_changed)
        body.text = String.format(getString(R.string.changed_the_room_info), admin)


        dialog.show()
    }

    private fun showPaymentInvalidDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val username = dialog.findViewById<TextView>(R.id.user_info)
        val amount = dialog.findViewById<TextView>(R.id.amount_info)
        val date = dialog.findViewById<TextView>(R.id.date_info)
        val description = dialog.findViewById<TextView>(R.id.description_info)
        val category = dialog.findViewById<TextView>(R.id.category_info)

        val decline = dialog.findViewById<Button>(R.id.decline_payment)
        val validate = dialog.findViewById<Button>(R.id.validate_payment)

        val user = users[notification.source_uid]!!
        val room = rooms[notification.room_uid]!!
        val paymentUid = notification.target_uid


        databaseReference.child(Constants.payments).child(room.uid).child(paymentUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val payment = snapshot.getValue<Payment>()!!
                title.text = getString(R.string.payment_validation)
                body.text = getString(R.string.payment_validation_body)
                username.text = user.username
                amount.text = payment.amount
                date.text = GetCurrentDate().dateFromTimestamp(payment.timestamp.toLong())
                description.text = payment.description
                category.text = payment.category



                decline.setOnClickListener {
                    if (payment.status == Constants.notify_payment_invalid){
                        databaseReference.child(Constants.payments).child(room.uid).child(paymentUid).child(Constants.status).setValue(Constants.payment_declined).addOnSuccessListener {
                            CreateNotification().create(room, Constants.notify_payment_declined, firebaseUser.uid, user.uid, paymentUid)
                            dialog.dismiss()
                        }
                    }
                    else {
                        Toast.makeText(this@Notifications, getString(R.string.action_already_taken), Toast.LENGTH_SHORT).show()
                    }


                }

                validate.setOnClickListener {
                    if (payment.status == Constants.notify_payment_invalid){
                        databaseReference.child(Constants.payments).child(room.uid).child(paymentUid).child(Constants.status).setValue(Constants.payment_valid).addOnSuccessListener {
                            CreateNotification().create(room, Constants.notify_payment_validated, firebaseUser.uid, user.uid, paymentUid)
                            dialog.dismiss()
                        }
                    }
                    else {
                        Toast.makeText(this@Notifications, getString(R.string.action_already_taken), Toast.LENGTH_SHORT).show()
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })



        dialog.show()
    }

    private fun showPaymentValidatedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username

        title.text = getString(R.string.payment_validation)
        if (notification.target_uid == firebaseUser.uid) {
            body.text = String.format(getString(R.string.your_invalid_payment_approved), admin)
        }

        else {
            body.text = String.format(getString(R.string.invalid_payment_approved), admin, user)
        }
        dialog.show()
    }

    private fun showPaymentDeclinedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username

        title.text = getString(R.string.payment_declined)

        if (notification.target_uid == firebaseUser.uid) {
            body.text = String.format(getString(R.string.your_invalid_payment_declined), admin)
        }

        else {
            body.text = String.format(getString(R.string.invalid_payment_declined), admin, user)
        }
        dialog.show()
    }

    private fun showRoomClosedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val room = rooms[notification.room_uid]!!

        title.text = getString(R.string.room_closed)

        if (notification.room_uid == firebaseUser.uid) {
            body.text = String.format(getString(R.string.you_closed_room), room.name)
        }

        else {
            body.text = String.format(getString(R.string.room_closed_by), admin, room.name)

        }
        dialog.show()
    }

    private fun showAdminDemotedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username
        val room = rooms[notification.room_uid]!!

        title.text = getString(R.string.admin_demoted)
        if (notification.source_uid == firebaseUser.uid) {
            body.text = String.format(getString(R.string.you_demoted_admin), user, room.name)
        }

        else {
            body.text = String.format(getString(R.string.admin_demoted_admin), admin, user, room.name)
        }
        dialog.show()
    }

    private fun showAdminPromotedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username
        val room = rooms[notification.room_uid]!!

        title.text = getString(R.string.resident_promoted)
        if (notification.source_uid == firebaseUser.uid) {
            body.text = String.format(getString(R.string.you_promoted_resident), user, room.name)
        }

        else {
            body.text = String.format(getString(R.string.admin_promoted_resident), admin, user)
        }
        dialog.show()
    }

    private fun showMOTDChangedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val user = users[notification.source_uid]!!.username
        val message = notification.extra

        title.text = getString(R.string.new_room_motd)
        body.text = String.format(getString(R.string.new_motd_plus), user, message)

        dialog.show()
    }

    private fun showUserRequestedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_actionable).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)
        val positive = dialog.findViewById<Button>(R.id.positive)
        val negative = dialog.findViewById<Button>(R.id.negative)


        val user = users[notification.source_uid]!!
        val room = rooms[notification.room_uid]!!


        title.text = getString(R.string.join_request)
        body.text = String.format(getString(R.string.request_to_join_room), user)

        positive.setOnClickListener {
            if (room.residents[user.uid] != Constants.added){
                databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(firebaseUser.uid).setValue(Constants.requested).addOnSuccessListener {
                    CreateNotification().create(room, Constants.notify_user_joined, firebaseUser.uid, user.uid, "")
                    dialog.dismiss()
                    Toast.makeText(this, getString(R.string.request_approved), Toast.LENGTH_SHORT).show()
                }
            }

        }

        dialog.show()
    }
}




package com.ezbalans.app.ezbalans.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.utils.Constants
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.adapters.NotificationsAdapter
import com.ezbalans.app.ezbalans.databinding.ViewNotificationsBinding
import com.ezbalans.app.ezbalans.utils.CreateNotification
import com.ezbalans.app.ezbalans.utils.GetCurrentDate
import com.ezbalans.app.ezbalans.utils.GetCustomDialog
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.viewmodels.NotificationsActivityViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity: AppCompatActivity(), NotificationsAdapter.OnItemClickListener {
    private lateinit var binding: ViewNotificationsBinding

    private val databaseReference = Firebase.database.reference
    private val firebaseUser = Firebase.auth.currentUser!!
    private var rooms = hashMapOf<String, Room>()
    private var users =  HashMap<String, User>()
    private val notifications =  ArrayList<Notification>()

    private val viewModel: NotificationsActivityViewModel by viewModels()
    lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewNotificationsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initViewModel()


    }

    private fun initViewModel(){
        viewModel.getAllUsers().observe(this, {
            users = it
        })

        viewModel.getAllRooms().observe(this, {
            rooms.clear()
            for (roomObject in it){
                rooms[roomObject.uid] = roomObject
            }
        })

        viewModel.getMyNotifications().observe(this, {
            notifications.clear()
            for (notification in it.values){
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
        })
    }

    private fun loadNotifications() {
        adapter = NotificationsAdapter(this, notifications, users, rooms, this)
        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(this)
    }

    override fun onItemClick(position: Int) {
        val notification = notifications[position]
        databaseReference.child(Constants.notifications).child(firebaseUser.uid).child(notification.uid).child(
            Constants.seen
        ).setValue(true).addOnSuccessListener {
            notification.seen = true

            adapter.notifyDataSetChanged()
        }

        when (notification.type) {
            Constants.notify_user_joined -> {
                showUserJoinedDialog(notification)
            }

            Constants.notify_user_declined -> {
                showUserDeclinedDialog(notification)
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
        val from = dialog.findViewById<TextView>(R.id.from)
        val to = dialog.findViewById<TextView>(R.id.to)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username
        val room = rooms[notification.room_uid]!!.name

        from.text = String.format(getString(R.string.action_by), admin)
        to.text = String.format(getString(R.string.action_to), user)


        if (notification.target_uid == firebaseUser.uid) {
            title.text = getString(R.string.welcome)
            body.text = String.format(getString(R.string.youre_added_to_room), admin, room)

            from.text = String.format(getString(R.string.action_by), admin)
            to.visibility = View.GONE

        }

        else {
            title.text = getString(R.string.new_resident_in_room)
            body.text = String.format(getString(R.string.added_to_room), admin, user)

            from.text = String.format(getString(R.string.action_by), admin)
            to.text = String.format(getString(R.string.action_to), user)
        }
        dialog.show()
    }

    private fun showUserDeclinedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)
        val from = dialog.findViewById<TextView>(R.id.from)
        val to = dialog.findViewById<TextView>(R.id.to)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username


        from.text = String.format(getString(R.string.action_by), admin)
        to.text = String.format(getString(R.string.action_to), user)

        title.text = getString(R.string.request_declined)
        body.text = getString(R.string.request_declined_body)

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

        val from = dialog.findViewById<TextView>(R.id.from)
        val to = dialog.findViewById<TextView>(R.id.to)
        from.text = String.format(getString(R.string.action_by), admin)
        to.text = String.format(getString(R.string.action_to), user)


        title.text = getString(R.string.resident_removed)
        body.text = getString(R.string.resident_removed_body)


        dialog.show()
    }

    private fun showRoomInfoChangedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username

        title.text = getString(R.string.room_info_changed)
        body.text = getString(R.string.change_to_room_info)

        val from = dialog.findViewById<TextView>(R.id.from)
        from.text = String.format(getString(R.string.action_by), admin)




        dialog.show()
    }

    private fun showPaymentInvalidDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_actionable).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val username = dialog.findViewById<TextView>(R.id.user_info)
        val amount = dialog.findViewById<TextView>(R.id.amount_info)
        val date = dialog.findViewById<TextView>(R.id.date_info)
        val description = dialog.findViewById<TextView>(R.id.description_info)
        val category = dialog.findViewById<TextView>(R.id.category_info)

        val decline = dialog.findViewById<Button>(R.id.negative)
        val validate = dialog.findViewById<Button>(R.id.positive)

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
                        databaseReference.child(Constants.payments).child(room.uid).child(paymentUid).child(
                            Constants.status
                        ).setValue(Constants.payment_declined).addOnSuccessListener {
                            CreateNotification().create(room,
                                Constants.notify_payment_declined, firebaseUser.uid, user.uid, paymentUid)
                            dialog.dismiss()
                        }
                    }
                    else {
                        Toast.makeText(this@NotificationsActivity, getString(R.string.action_already_taken), Toast.LENGTH_SHORT).show()
                    }


                }

                validate.setOnClickListener {
                    if (payment.status == Constants.notify_payment_invalid){
                        databaseReference.child(Constants.payments).child(room.uid).child(paymentUid).child(
                            Constants.status
                        ).setValue(Constants.payment_valid).addOnSuccessListener {
                            CreateNotification().create(room,
                                Constants.notify_payment_validated, firebaseUser.uid, user.uid, paymentUid)
                            dialog.dismiss()
                        }
                    }
                    else {
                        Toast.makeText(this@NotificationsActivity, getString(R.string.action_already_taken), Toast.LENGTH_SHORT).show()
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
        val from = dialog.findViewById<TextView>(R.id.from)
        val to = dialog.findViewById<TextView>(R.id.to)
        from.text = String.format(getString(R.string.action_by), admin)
        to.text = String.format(getString(R.string.action_to), user)


        body.text = getString(R.string.invalid_payment_approved)

        dialog.show()
    }

    private fun showPaymentDeclinedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username

        title.text = getString(R.string.payment_declined)
        val from = dialog.findViewById<TextView>(R.id.from)
        val to = dialog.findViewById<TextView>(R.id.to)
        from.text = String.format(getString(R.string.action_by), admin)
        to.text = String.format(getString(R.string.action_to), user)

        body.text = getString(R.string.invalid_payment_declined)

        dialog.show()
    }

    private fun showRoomClosedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username

        title.text = getString(R.string.room_closed)

        val from = dialog.findViewById<TextView>(R.id.from)
        from.text = String.format(getString(R.string.action_by), admin)
        body.text = getString(R.string.room_closed_body)


        dialog.show()
    }

    private fun showAdminDemotedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username

        val from = dialog.findViewById<TextView>(R.id.from)
        val to = dialog.findViewById<TextView>(R.id.to)
        from.text = String.format(getString(R.string.action_by), admin)
        to.text = String.format(getString(R.string.action_to), user)


        title.text = getString(R.string.admin_demoted)
        body.text = getString(R.string.demoted_resident)

        dialog.show()
    }

    private fun showAdminPromotedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val admin = users[notification.source_uid]!!.username
        val user = users[notification.target_uid]!!.username

        val from = dialog.findViewById<TextView>(R.id.from)
        val to = dialog.findViewById<TextView>(R.id.to)
        from.text = String.format(getString(R.string.action_by), admin)
        to.text = String.format(getString(R.string.action_to), user)


        title.text = getString(R.string.resident_promoted)
        body.text = getString(R.string.promoted_resident)

        dialog.show()
    }

    private fun showMOTDChangedDialog(notification: Notification){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_notification_informative).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val body = dialog.findViewById<TextView>(R.id.body)

        val user = users[notification.source_uid]!!.username
        val message = notification.extra

        val from = dialog.findViewById<TextView>(R.id.from)
        from.text = String.format(getString(R.string.action_by), user)


        title.text = getString(R.string.new_room_motd)
        body.text = message

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
        body.text = String.format(getString(R.string.request_to_join_room))


        val from = dialog.findViewById<TextView>(R.id.from)
        from.text = String.format(getString(R.string.action_by), user.username)


        if (room.residents[user.uid] == Constants.requested){
            positive.setOnClickListener {
                    databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(user.uid).setValue(
                        Constants.added
                    ).addOnSuccessListener {
                        CreateNotification().create(room,
                            Constants.notify_user_joined, firebaseUser.uid, user.uid, "")
                        dialog.dismiss()
                        Toast.makeText(this, getString(R.string.request_approved), Toast.LENGTH_SHORT).show()
                    }
            }
            negative.setOnClickListener {
                    databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(user.uid).setValue(
                        Constants.declined
                    ).addOnSuccessListener {
                        CreateNotification().create(room,
                            Constants.notify_user_declined, firebaseUser.uid, user.uid, "")
                        dialog.dismiss()
                        Toast.makeText(this, getString(R.string.request_declined), Toast.LENGTH_SHORT).show()
                    }

            }
        }
        else{
            positive.alpha = 0.5f
            negative.alpha = 0.5f
            positive.isClickable = false
            negative.isClickable = false
        }




        dialog.show()
    }
}




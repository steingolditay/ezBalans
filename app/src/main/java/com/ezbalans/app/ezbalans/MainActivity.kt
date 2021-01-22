package com.ezbalans.app.ezbalans

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.databinding.ViewSplashScreenBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ViewSplashScreenBinding
    private var numberOfMethods = 0
    private var counter = 0

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewSplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PowerPreference.init(this)

        val firebaseUser = Firebase.auth.currentUser
        numberOfMethods = if(firebaseUser!=null) 6 else 1

        createPrefs(firebaseUser)

    }

    private fun createPrefs(firebaseUser: FirebaseUser?){
        val databaseReference = Firebase.database.reference
        val users = hashMapOf<String, User>()
        val notifications = hashMapOf<String, Notification>()
        val rooms = hashMapOf<String, Room>()
        val myRooms = hashMapOf<String, Room>()
        val allShoppingLists = hashMapOf<String, HashMap<String, Boolean>>()
        val payments = hashMapOf<String, Payment>()
        val myPayments = hashMapOf<String, Payment>()
        val myBudgets = hashMapOf<String, Int>()


        // GET ALL USERS
        databaseReference.child(Constants.users).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (entry in snapshot.children) {
                    val user = entry.getValue<User>()!!
                    users[user.uid] = user
                }
                PowerPreference.getDefaultFile().putObject(Constants.users, users)
                counter += 1
                moveToApp(firebaseUser)

                if (firebaseUser!= null){
                    // GET MY NOTIFICATIONS
                    databaseReference.child(Constants.notifications).child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (entry in snapshot.children){
                                val notification = entry.getValue<Notification>()!!
                                notifications[notification.uid] = notification

                            }
                            PowerPreference.getDefaultFile().putObject(Constants.notifications, notifications)
                            counter += 1
                            moveToApp(firebaseUser)
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
                    // GET ALL SHOPPING LISTS
                    databaseReference.child(Constants.shopping_lists).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (data in snapshot.children){
                                val key = data.key!!
                                val value = data.getValue<HashMap<String, Boolean>>()!!
                                allShoppingLists[key] = value

                            }
                            PowerPreference.getDefaultFile().putObject(Constants.shopping_lists, allShoppingLists)
                            counter += 1
                            moveToApp(firebaseUser)

                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })

                    // GET MY BUDGETS
                    databaseReference.child(Constants.budgets).child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (data in snapshot.children){
                                val key = data.key!!
                                val value = data.value.toString().toInt()
                                myBudgets[key] = value
                            }
                            PowerPreference.getDefaultFile().putObject(Constants.budgets, myBudgets)
                            counter += 1
                            moveToApp(firebaseUser)

                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })

                    // GET ROOMS PAYMENTS & MY PAYMENTS
                    databaseReference.child(Constants.payments).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (entry in snapshot.children) {
                                // val roomUid = entry.key
                                entry.children.forEach {
                                    val payment = it.getValue<Payment>()!!
                                    payments[payment.payment_uid] = payment
                                    if (payment.from == firebaseUser.uid){
                                        myPayments[payment.payment_uid] = payment
                                    }
                                }
                            }
                            PowerPreference.getDefaultFile().putObject(Constants.payments, payments)
                            PowerPreference.getDefaultFile().putObject(Constants.my_payments, myPayments)
                            counter += 1
                            moveToApp(firebaseUser)

                        }
                        override fun onCancelled(error: DatabaseError) {
                        }

                    })

                    // GET ALL ROOMS,  MY ROOMS & SHOPPING LISTS
                    databaseReference.child(Constants.rooms).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (entry in snapshot.children){
                                val room = entry.getValue<Room>()!!
                                rooms[room.uid] = room

                                if (room.residents.containsKey(firebaseUser.uid)){
                                    myRooms[room.uid] = room
                                }
                            }
                            PowerPreference.getDefaultFile().putObject(Constants.rooms, rooms)
                            PowerPreference.getDefaultFile().putObject(Constants.my_rooms, rooms)
                            counter += 1
                            moveToApp(firebaseUser)

                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })







    }

    private fun moveToApp(firebaseUser: FirebaseUser?) {
        if (counter == numberOfMethods){
            PowerPreference.getDefaultFile().setInt(Constants.main_activity_count, 0)
            PowerPreference.getDefaultFile().setInt(Constants.room_activity_count, 0)
            when {
                PowerPreference.getDefaultFile().getBoolean(Constants.first_time, true) -> {
                    val intent = Intent(this, AppIntro::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                // go to home activity
                firebaseUser != null -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                // go to signin activity
                else -> {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
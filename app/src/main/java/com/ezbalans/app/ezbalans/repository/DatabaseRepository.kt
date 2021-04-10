package com.ezbalans.app.ezbalans.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.utils.Constants
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.utils.CreateNotification
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseRepository
@Inject constructor() {

    private val databaseReference = Firebase.database.reference
    private var firebaseUser: FirebaseUser? = Firebase.auth.currentUser

    private val roomsArray = ArrayList<Room>()
    private val myRoomsArray = ArrayList<Room>()
    private val roomsKeysArray = ArrayList<String>()
    private val myRoomsKeysArray = ArrayList<String>()
    private val usersMap = hashMapOf<String, User>()
    private val userKeysArray = ArrayList<String>()
    val notificationsMap = hashMapOf<String, Notification>()
    val paymentsMap = hashMapOf<String, Payment>()
    val myPaymentsMap = hashMapOf<String, Payment>()
    val myBudgetsMap = hashMapOf<String, Int>()
    val shoppingListsMap = hashMapOf<String, HashMap<String, Boolean>>()

    private var rooms = MutableLiveData<List<Room>>()
    private var roomKeys = MutableLiveData<List<String>>()
    private var myRooms = MutableLiveData<List<Room>>()
    private var myRoomKeys = MutableLiveData<List<String>>()
    private var users = MutableLiveData<HashMap<String, User>>()
    private var userKeys = MutableLiveData<List<String>>()
    private var notifications = MutableLiveData<HashMap<String, Notification>>()
    private var payments = MutableLiveData<HashMap<String, Payment>>()
    private var myPayments = MutableLiveData<HashMap<String, Payment>>()
    private var myBudgets = MutableLiveData<HashMap<String, Int>>()
    private var shoppingLists = MutableLiveData<HashMap<String, HashMap<String, Boolean>>>()

    lateinit var roomsValueEventListener:  ValueEventListener
    lateinit var usersValueEventListener:  ValueEventListener
    lateinit var paymentsValueEventListener:  ValueEventListener
    lateinit var shoppingListsValueEventListener:  ValueEventListener
    lateinit var budgetsValueEventListener:  ValueEventListener
    lateinit var notificationsValueEventListener:  ValueEventListener

    private val roomsRef = databaseReference.child(Constants.rooms)
    private val usersRef = databaseReference.child(Constants.users)
    private val notificationsRef = databaseReference.child(Constants.notifications)
    private val paymentsRef = databaseReference.child(Constants.payments)
    private val budgetsRef = databaseReference.child(Constants.budgets)
    private val shoppingListsRef = databaseReference.child(Constants.shopping_lists)

    init {
        if (firebaseUser != null) {
            addListeners()
        }
    }

    fun addListeners() {
        updateFirebaseUser()
        getUserData()
        if (firebaseUser != null){
            getBudgetsData()
            getNotificationsData()
            getPaymentsData()
            getShoppingListsData()
            getRoomsData()
        }
    }

    fun removeListeners(){
        if (firebaseUser != null){
            roomsRef.removeEventListener(roomsValueEventListener)
            usersRef.removeEventListener(usersValueEventListener)
            notificationsRef.child(firebaseUser!!.uid).removeEventListener(notificationsValueEventListener)
            paymentsRef.removeEventListener(paymentsValueEventListener)
            budgetsRef.child(firebaseUser!!.uid).removeEventListener(budgetsValueEventListener)
            shoppingListsRef.removeEventListener(shoppingListsValueEventListener)
        }
        updateFirebaseUser()

    }

    private fun updateFirebaseUser(){
        firebaseUser = Firebase.auth.currentUser
    }

    @Singleton
    @Provides
    fun provideAllRooms(): MutableLiveData<List<Room>> {
        return rooms
    }

    @Singleton
    @Provides
    fun provideAllRoomKeys(): MutableLiveData<List<String>> {
        return roomKeys
    }

    @Singleton
    @Provides
    fun provideMyRooms(): MutableLiveData<List<Room>> {
        return myRooms
    }

    @Singleton
    @Provides
    fun provideMyRoomKeys(): MutableLiveData<List<String>> {
        return userKeys
    }

    @Singleton
    @Provides
    fun provideAllUsers(): MutableLiveData<HashMap<String, User>> {
        return users
    }

    @Singleton
    @Provides
    fun provideMyNotifications(): MutableLiveData<HashMap<String, Notification>> {
        return notifications
    }

    @Singleton
    @Provides
    fun provideAllPayments(): MutableLiveData<HashMap<String, Payment>> {
        return payments
    }

    @Singleton
    @Provides
    fun provideMyPayments(): MutableLiveData<HashMap<String, Payment>> {
        return myPayments
    }

    @Singleton
    @Provides
    fun provideMyBudgets(): MutableLiveData<HashMap<String, Int>> {
        return myBudgets
    }

    @Singleton
    @Provides
    fun provideMyShoppingLists(): MutableLiveData<HashMap<String, HashMap<String, Boolean>>> {
        return shoppingLists
    }

    private fun getRoomsData() {
        roomsValueEventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                roomsArray.clear()
                roomsKeysArray.clear()
                myRoomsArray.clear()
                myRoomsKeysArray.clear()
                for (entry in snapshot.children) {
                    val room = entry.getValue<Room>()!!
                    roomsArray.add(room)
                    roomsKeysArray.add(room.identity_key)
                    if (room.residents.containsKey(firebaseUser?.uid)) {
                        if (room.residents[firebaseUser?.uid] == Constants.added) {
                            myRoomsArray.add(room)
                            myRoomsKeysArray.add(room.identity_key)
                        }
                    }
                }
                rooms.value = roomsArray
                roomKeys.value = roomsKeysArray
                myRooms.value = myRoomsArray
                myRoomKeys.value = myRoomsKeysArray
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

       roomsRef.addValueEventListener(roomsValueEventListener)

    }

    private fun getUserData() {
        usersValueEventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                usersMap.clear()
                for (entry in snapshot.children) {
                    val user = entry.getValue<User>()!!
                    usersMap[user.uid] = user
                    userKeysArray.add(user.identity_key)
                }
                users.value = usersMap
                userKeys.value = userKeysArray
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }
        usersRef.addValueEventListener(usersValueEventListener)
    }

    private fun getNotificationsData() {
        notificationsValueEventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationsMap.clear()
                for (entry in snapshot.children) {
                    val notification = entry.getValue<Notification>()!!
                    notificationsMap[notification.uid] = notification
                }
                notifications.postValue(notificationsMap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }
        notificationsRef.child(firebaseUser!!.uid).addValueEventListener(notificationsValueEventListener)
    }

    private fun getPaymentsData() {
        paymentsValueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                paymentsMap.clear()
                myPaymentsMap.clear()
                for (entry in snapshot.children) {
                    entry.children.forEach {
                        val payment = it.getValue<Payment>()!!
                        paymentsMap[payment.payment_uid] = payment
                        if (firebaseUser != null){
                            if (payment.from == firebaseUser!!.uid) {
                                myPaymentsMap[payment.payment_uid] = payment
                            }
                        }
                    }
                }
                payments.postValue(paymentsMap)
                myPayments.postValue(myPaymentsMap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }

        paymentsRef.addValueEventListener(paymentsValueEventListener)



    }

    private fun getBudgetsData() {
        budgetsValueEventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                myBudgetsMap.clear()
                for (data in snapshot.children) {
                    myBudgetsMap[data.key!!] = data.getValue<Int>()!!
                }
                myBudgets.postValue(myBudgetsMap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }
        budgetsRef.child(firebaseUser!!.uid).addValueEventListener(budgetsValueEventListener)
    }

    private fun getShoppingListsData() {
        shoppingListsValueEventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                shoppingListsMap.clear()
                for (data in snapshot.children) {
                    val key = data.key!!
                    val value = data.getValue<HashMap<String, Boolean>>()!!
                    shoppingListsMap[key] = value

                }
                shoppingLists.postValue(shoppingListsMap)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }
        shoppingListsRef.addValueEventListener(shoppingListsValueEventListener)
    }

    fun roomCreatedUpdate(context: Context, room: Room) {
        var counter = 0

        for (user in room.residents.keys) {
            // add default budgets for the room residents
            databaseReference.child(Constants.budgets).child(user).child(room.uid).setValue(0).addOnSuccessListener {
                counter += 1
                // when finished with all residents
                // create room shopping list
                if (counter == (room.residents.size)) {

                    databaseReference.child(Constants.shopping_lists).child(room.shopping_list).child(context.getString(R.string.example_item)).setValue(true).addOnCompleteListener {
                        // create room
                        databaseReference.child(Constants.rooms).child(room.uid).setValue(room).addOnSuccessListener {
                            // send notifications for all residents
                            for (userUid in room.residents.keys) {
                                CreateNotification().create(room, Constants.notify_user_joined, firebaseUser!!.uid, user, "")
                            }
                        }
                    }

                }
            }
        }
    }

    fun addUserToRoom(user: String, room: Room) {
        databaseReference.child(Constants.budgets).child(user).child(room.uid).setValue(0).addOnSuccessListener {
            databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(user).setValue(Constants.added)
                CreateNotification().create(room, Constants.notify_user_joined, firebaseUser!!.uid, user, "")
        }
    }

    fun declineUserJoin(user: String, room: Room){
        databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(user).setValue(Constants.declined).addOnSuccessListener {
            CreateNotification().create(room, Constants.notify_user_declined, firebaseUser!!.uid, user, "")
        }
    }
}

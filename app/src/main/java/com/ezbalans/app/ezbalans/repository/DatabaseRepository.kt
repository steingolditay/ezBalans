package com.ezbalans.app.ezbalans.repository

import androidx.lifecycle.MutableLiveData
import com.ezbalans.app.ezbalans.helpers.Constants
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
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

@Inject constructor(){
    private val databaseReference = Firebase.database.reference
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

    val firebaseUser: FirebaseUser? = Firebase.auth.currentUser
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


    init {
        getRoomsData()
        getUserData()
        getNotificationsData()
        getPaymentsData()
        getBudgetsData()
        getShoppingListsData()
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
    fun provideAllUsers(): MutableLiveData<HashMap<String, User>>{
        return users
    }

    @Singleton
    @Provides
    fun provideMyNotifications(): MutableLiveData<HashMap<String, Notification>> {
        return notifications
    }

    @Singleton
    @Provides
    fun provideAllPayments(): MutableLiveData<HashMap<String, Payment>>{
        return payments
    }

    @Singleton
    @Provides
    fun provideMyPayments(): MutableLiveData<HashMap<String, Payment>>{
        return myPayments
    }

    @Singleton
    @Provides
    fun provideMyBudgets(): MutableLiveData<HashMap<String, Int>> {
        return myBudgets
    }

    @Singleton
    @Provides
    fun provideMyShoppingLists(): MutableLiveData<HashMap<String, HashMap<String, Boolean>>>{
        return shoppingLists
    }


    private fun getRoomsData(){
        databaseReference.child(Constants.rooms).addValueEventListener(object : ValueEventListener {
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

        })
    }

    private fun getUserData(){
        databaseReference.child(Constants.users).addValueEventListener(object : ValueEventListener {
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

        })
    }

    private fun getNotificationsData(){
        databaseReference.child(Constants.notifications).child(firebaseUser!!.uid).addValueEventListener(object : ValueEventListener {
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
        })
    }

    private fun getPaymentsData(){
        databaseReference.child(Constants.payments).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                paymentsMap.clear()
                myPaymentsMap.clear()
                for (entry in snapshot.children) {
//                        val roomUid = entry.key
                    entry.children.forEach {
                        val payment = it.getValue<Payment>()!!
                        paymentsMap[payment.payment_uid] = payment
                        if (payment.from == firebaseUser!!.uid) {
                            myPaymentsMap[payment.payment_uid] = payment
                        }
                    }
                }
                payments.postValue(paymentsMap)
                myPayments.postValue(myPaymentsMap)

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getBudgetsData(){
        databaseReference.child(Constants.budgets).child(firebaseUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                myBudgetsMap.clear()
                for (data in snapshot.children) {
                    myBudgetsMap[data.key!!] = data.getValue<Int>()!!
                }
                myBudgets.postValue(myBudgetsMap)

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getShoppingListsData(){
        databaseReference.child(Constants.shopping_lists).addValueEventListener(object : ValueEventListener {
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

        })
    }

}
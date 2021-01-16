package com.ezbalans.app.ezbalans


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.homeFragments.*
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.databinding.ViewMainframeBinding
import com.ezbalans.app.ezbalans.eventBus.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class HomeActivity: AppCompatActivity(){
    private lateinit var binding: ViewMainframeBinding

    private lateinit var currentFragment:Fragment
    private lateinit var fragmentHome: FragmentHome
    private lateinit var fragmentRooms: FragmentRooms
    private lateinit var fragmentBudgets: FragmentWallet

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }

    override fun onResume() {
        super.onResume()
        val count = PowerPreference.getDefaultFile().getInt(Constants.main_activity_count)
        val newCount = count+1
        PowerPreference.getDefaultFile().setInt(Constants.main_activity_count, newCount)

        if (newCount > 1){
            when (currentFragment) {
                fragmentHome -> {
                    updateFragment(fragmentHome)
                }
                fragmentRooms -> {
                    updateFragment(fragmentRooms)
                }
                fragmentBudgets -> {
                    updateFragment(fragmentBudgets)
                }
            }
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewMainframeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PowerPreference.init(this)

        loadMainListeners()
        // don't load fragments if user is logged out
        // so it wont interfere with trying deep-link
        if (Firebase.auth.currentUser != null){
            currentFragment = Fragment()
            fragmentHome = FragmentHome()
            fragmentRooms = FragmentRooms()
            fragmentBudgets = FragmentWallet()
        }

        // check if deep linked to join room
        val uri = intent.data
        if (uri != null){
            if (Firebase.auth.currentUser != null){

                val joinFragmentRooms = FragmentRooms()

                val roomUid = uri.lastPathSegment!!

                val fragmentBundle = Bundle();
                fragmentBundle.putString(Constants.room_uid, roomUid)
                joinFragmentRooms.arguments = fragmentBundle
                setFragment(joinFragmentRooms, Constants.rooms_tag)
            }
            else {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }
        else {
            setFragment(fragmentHome, Constants.home_tag)
        }


        binding.bottomBar.onItemSelected = {
            when (it) {
                0 -> {
                    setFragment(fragmentHome, Constants.home_tag)
                    currentFragment = fragmentHome
                }
                1 -> {
                    setFragment(fragmentRooms, Constants.rooms_tag);
                    currentFragment = fragmentRooms
                }
                2 -> {
                    setFragment(fragmentBudgets, Constants.budgets_tag)
                    currentFragment = fragmentBudgets
                }

            }
        }

        Firebase.auth.addAuthStateListener(object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {

            }

        })

    }

    @Subscribe
    fun onUsersUpdate(event: UsersEvent){
//        Log.d("TAG", "onUsersUpdate: HomeActivity")
    }

    @Subscribe
    fun onNotificationsUpdate(event: NotificationsEvent){
//        Log.d("TAG", "onNotificationsUpdate: HomeActivity")
    }

    @Subscribe
    fun onPaymentsUpdate(event: PaymentsEvent){
//        Log.d("TAG", "onPaymentsUpdate: HomeActivity")
    }

    @Subscribe
    fun onRoomsUpdate(event: RoomsEvent){
//        Log.d("TAG", "onRoomsUpdate: HomeActivity")
    }
    @Subscribe
    fun onBudgetsUpdate(event: RoomsEvent){
//        Log.d("TAG", "onBudgetsUpdate: HomeActivity")

    }

    @Subscribe
    fun onShoppingListsUpdate(event: ShoppingListsEvent){
//        Log.d("TAG", "onBudgetsUpdate: HomeActivity")

    }

    private fun setFragment(fragment: Fragment, tag: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainframe, fragment, tag);
        fragmentTransaction.commit();

    }

    private fun updateFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.detach(fragment)
        fragmentTransaction.attach(fragment)
        fragmentTransaction.commitNow()
    }

    private fun loadMainListeners(){
        val databaseReference = Firebase.database.reference
        val users = hashMapOf<String, User>()
        val notifications = hashMapOf<String, Notification>()
        val rooms = hashMapOf<String, Room>()
        val myRooms = hashMapOf<String, Room>()
        val shoppingLists = hashMapOf<String, HashMap<String, Boolean>>()
        val payments = hashMapOf<String, Payment>()
        val myPayments = hashMapOf<String, Payment>()
        val myBudgets = hashMapOf<String, Int>()


        // GET ALL USERS
        databaseReference.child(Constants.users).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (entry in snapshot.children) {
                    val user = entry.getValue<User>()!!
                    users[user.uid] = user

                }
                PowerPreference.getDefaultFile().putObject(Constants.users, users)
                EventBus.getDefault().post(UsersEvent())
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        // GET MY NOTIFICATIONS
        if (Firebase.auth.currentUser != null){
            val firebaseUser = Firebase.auth.currentUser!!
            databaseReference.child(Constants.notifications).child(firebaseUser.uid).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (entry in snapshot.children){
                        val notification = entry.getValue<Notification>()!!
                        notifications[notification.uid] = notification

                    }
                    PowerPreference.getDefaultFile().putObject(Constants.notifications, notifications)
                    EventBus.getDefault().post(NotificationsEvent())



                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        }

        // GET ALL ROOMS,  MY ROOMS & SHOPPING LISTS
        if (Firebase.auth.currentUser != null){
            val firebaseUser = Firebase.auth.currentUser!!
            databaseReference.child(Constants.rooms).addValueEventListener(object : ValueEventListener{
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
                    EventBus.getDefault().post(RoomsEvent())


                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        }
        // GET ROOMS PAYMENTS & MY PAYMENTS
        if (Firebase.auth.currentUser != null){
            val firebaseUser = Firebase.auth.currentUser!!
            databaseReference.child(Constants.payments).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (entry in snapshot.children) {
//                        val roomUid = entry.key
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
                    EventBus.getDefault().post(PaymentsEvent())


                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        }
        // GET MY BUDGETS
        if (Firebase.auth.currentUser != null){
            val firebaseUser = Firebase.auth.currentUser!!
            databaseReference.child(Constants.budgets).child(firebaseUser.uid).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children){
                        myBudgets[data.key!!] = data.getValue<Int>()!!
                    }
                    PowerPreference.getDefaultFile().putObject(Constants.budgets, myBudgets)
                    EventBus.getDefault().post(BudgetsEvent())
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        // GET ALL SHOPPING LISTS
        databaseReference.child(Constants.shopping_lists).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children){
                    val key = data.key!!
                    val value = data.getValue<HashMap<String, Boolean>>()!!
                    shoppingLists[key] = value

                }
                PowerPreference.getDefaultFile().putObject(Constants.shopping_lists, shoppingLists)
                EventBus.getDefault().post(ShoppingListsEvent())

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onBackPressed() {
        if (!fragmentHome.isVisible){
            setFragment(fragmentHome, Constants.home_tag)
            binding.bottomBar.itemActiveIndex = 0
        }
        else{
            super.onBackPressed()
        }

    }


}
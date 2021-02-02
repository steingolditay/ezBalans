package com.ezbalans.app.ezbalans


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.homeFragments.*
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.ezbalans.app.ezbalans.databinding.ViewMainframeBinding
import com.ezbalans.app.ezbalans.eventBus.*
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

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ViewMainframeBinding

    private lateinit var currentFragment: Fragment
    private lateinit var fragmentProfile: FragmentProfile
    private lateinit var fragmentRooms: FragmentRooms
    private lateinit var fragmentBudgets: FragmentWallet
    var fragmentCount = 0

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
        val newCount = count + 1
        PowerPreference.getDefaultFile().setInt(Constants.main_activity_count, newCount)

        if (newCount > 1) {
            when (currentFragment) {
                fragmentProfile -> {
                    updateFragment(fragmentProfile)
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
        if (Firebase.auth.currentUser != null) {
            currentFragment = Fragment()
            fragmentProfile = FragmentProfile()
            fragmentRooms = FragmentRooms()
            fragmentBudgets = FragmentWallet()
        }

        // check if deep linked to join room
        val uri = intent.data
        if (uri != null) {
            if (Firebase.auth.currentUser != null) {

                val joinFragmentRooms = FragmentRooms()

                val roomUid = uri.lastPathSegment!!

                val fragmentBundle = Bundle();
                fragmentBundle.putString(Constants.room_uid, roomUid)
                joinFragmentRooms.arguments = fragmentBundle
                setFragment(joinFragmentRooms, Constants.rooms_tag)
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }
        else {
        }

        binding.bottomBar.onItemSelected = {
            when (it) {
                0 -> {
                    setFragment(fragmentRooms, Constants.rooms_tag)
                    currentFragment = fragmentRooms
                }
                1 -> {
                    setFragment(fragmentBudgets, Constants.budgets_tag);
                    currentFragment = fragmentBudgets
                }
                2 -> {
                    setFragment(fragmentProfile, Constants.profile_tag)
                    currentFragment = fragmentProfile
                }

            }
        }

    }

    @Subscribe
    fun onUsersUpdate(event: UsersEvent) {
//        Log.d("TAG", "onUsersUpdate: HomeActivity")
    }

    @Subscribe
    fun onNotificationsUpdate(event: NotificationsEvent) {
        Log.d("TAG", "onNotificationsUpdate: HomeActivity")
    }

    @Subscribe
    fun onPaymentsUpdate(event: PaymentsEvent) {
//        Log.d("TAG", "onPaymentsUpdate: HomeActivity")
    }

    @Subscribe
    fun onRoomsUpdate(event: RoomsEvent) {
//        Log.d("TAG", "onRoomsUpdate: HomeActivity")
    }

    @Subscribe
    fun onBudgetsUpdate(event: RoomsEvent) {
//        Log.d("TAG", "onBudgetsUpdate: HomeActivity")

    }

    @Subscribe
    fun onShoppingListsUpdate(event: ShoppingListsEvent) {
//        Log.d("TAG", "onBudgetsUpdate: HomeActivity")

    }


    private fun setMainFragment(){
        if (fragmentCount == 6){
            setFragment(fragmentRooms, Constants.rooms_tag)

        }

    }


    private fun setFragment(fragment: Fragment, tag: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainframe, fragment, tag);
        fragmentTransaction.commit();

    }

    private fun updateFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.detach(fragment)
        fragmentTransaction.attach(fragment)
        fragmentTransaction.commitNow()
    }

    private fun loadMainListeners() {
        val databaseReference = Firebase.database.reference
        val firebaseUser = Firebase.auth.currentUser!!

        val users = hashMapOf<String, User>()
        val notifications = hashMapOf<String, Notification>()
        val rooms = hashMapOf<String, Room>()
        val myRooms = hashMapOf<String, Room>()
        val shoppingLists = hashMapOf<String, HashMap<String, Boolean>>()
        val payments = hashMapOf<String, Payment>()
        val myPayments = hashMapOf<String, Payment>()
        val myBudgets = hashMapOf<String, Int>()


        // GET ALL USERS
        databaseReference.child(Constants.users).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (entry in snapshot.children) {
                    val user = entry.getValue<User>()!!
                    users[user.uid] = user

                }
                PowerPreference.getDefaultFile().setObject(Constants.users, users)
                fragmentCount += 1
                setMainFragment()
                EventBus.getDefault().post(UsersEvent())
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


        // GET MY NOTIFICATIONS
        databaseReference.child(Constants.notifications).child(firebaseUser.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notifications.clear()
                    for (entry in snapshot.children) {
                        val notification = entry.getValue<Notification>()!!
                        notifications[notification.uid] = notification
                    }
                    PowerPreference.getDefaultFile().setObject(Constants.notifications, notifications)
                    fragmentCount += 1
                    setMainFragment()

                    EventBus.getDefault().post(NotificationsEvent())
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


        // GET ALL ROOMS,  MY ROOMS & SHOPPING LISTS
        databaseReference.child(Constants.rooms).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rooms.clear()
                myRooms.clear()
                for (entry in snapshot.children) {
                    val room = entry.getValue<Room>()!!
                    rooms[room.uid] = room
                    if (room.residents.containsKey(firebaseUser.uid)) {
                        myRooms[room.uid] = room
                    }
                }
                PowerPreference.getDefaultFile().setObject(Constants.rooms, rooms)
                PowerPreference.getDefaultFile().setObject(Constants.my_rooms, myRooms)
                fragmentCount += 1
                setMainFragment()

                EventBus.getDefault().post(RoomsEvent())
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


        // GET ROOMS PAYMENTS & MY PAYMENTS
        databaseReference.child(Constants.payments).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                payments.clear()
                myPayments.clear()
                for (entry in snapshot.children) {
//                        val roomUid = entry.key
                    entry.children.forEach {
                        val payment = it.getValue<Payment>()!!
                        payments[payment.payment_uid] = payment
                        if (payment.from == firebaseUser.uid) {
                            myPayments[payment.payment_uid] = payment
                        }
                    }
                }
                PowerPreference.getDefaultFile().setObject(Constants.payments, payments)
                PowerPreference.getDefaultFile().setObject(Constants.my_payments, myPayments)
                fragmentCount += 1
                setMainFragment()

                EventBus.getDefault().post(PaymentsEvent())
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


        // GET MY BUDGETS
            databaseReference.child(Constants.budgets).child(firebaseUser.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        myBudgets.clear()
                        for (data in snapshot.children) {
                            myBudgets[data.key!!] = data.getValue<Int>()!!
                        }
                        PowerPreference.getDefaultFile().setObject(Constants.budgets, myBudgets)
                        fragmentCount += 1
                        setMainFragment()

                        EventBus.getDefault().post(BudgetsEvent())
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })


        // GET ALL SHOPPING LISTS
        databaseReference.child(Constants.shopping_lists)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    shoppingLists.clear()
                    for (data in snapshot.children) {
                        val key = data.key!!
                        val value = data.getValue<HashMap<String, Boolean>>()!!
                        shoppingLists[key] = value

                    }
                    PowerPreference.getDefaultFile().setObject(Constants.shopping_lists, shoppingLists)
                    fragmentCount += 1
                    setMainFragment()

                    EventBus.getDefault().post(ShoppingListsEvent())

                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    override fun onBackPressed() {
        if (!fragmentRooms.isVisible) {
            setFragment(fragmentRooms, Constants.rooms_tag)
            binding.bottomBar.itemActiveIndex = 0
        } else {
            super.onBackPressed()
        }

    }


}
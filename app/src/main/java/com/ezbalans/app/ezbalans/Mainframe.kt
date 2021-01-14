package com.ezbalans.app.ezbalans


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.Mainframe_Fragments.*
import com.ezbalans.app.ezbalans.databinding.ViewMainframeBinding
import com.ezbalans.app.ezbalans.databinding.ViewWelcomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference

class Mainframe: AppCompatActivity(){
    private lateinit var binding: ViewMainframeBinding

    private lateinit var currentFragment:Fragment
    private lateinit var fragmentHome: FragmentHome
    private lateinit var fragmentRooms: FragmentRooms
    private lateinit var fragmentBudgets: FragmentWallet


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

    override fun onBackPressed() {
        if (!fragmentHome.isVisible){
            setFragment(fragmentHome, Constants.home_tag)
            binding.bottomBar.itemActiveIndex = 0
//            bottom_bar.barBackgroundColor = getColor(R.color.colorPrimary)
        }
        else{
            super.onBackPressed()
        }

    }


}
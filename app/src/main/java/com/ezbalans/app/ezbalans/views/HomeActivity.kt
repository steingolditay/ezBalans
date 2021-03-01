package com.ezbalans.app.ezbalans.views


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.helpers.Constants
import com.ezbalans.app.ezbalans.helpers.LocaleManager
import com.ezbalans.app.ezbalans.MainActivity
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.views.homeFragments.*
import com.ezbalans.app.ezbalans.databinding.ViewMainframeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ViewMainframeBinding

    private lateinit var currentFragment: Fragment
    private lateinit var fragmentProfile: FragmentProfile
    private lateinit var fragmentRooms: FragmentRooms
    private lateinit var fragmentBudgets: FragmentWallet



    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewMainframeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        PowerPreference.init(this)

//        loadMainListeners()
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
            }
            else {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }
        else {
            setFragment(fragmentRooms, Constants.rooms_tag)
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


    override fun onBackPressed() {
        if (!fragmentRooms.isVisible) {
            setFragment(fragmentRooms, Constants.rooms_tag)
            binding.bottomBar.itemActiveIndex = 0
        } else {
            super.onBackPressed()
        }

    }


}
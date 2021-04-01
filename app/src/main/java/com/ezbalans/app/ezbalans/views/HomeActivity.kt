package com.ezbalans.app.ezbalans.views


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ezbalans.app.ezbalans.utils.Constants
import com.ezbalans.app.ezbalans.utils.LocaleManager
import com.ezbalans.app.ezbalans.MainActivity
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.views.homeFragments.*
import com.ezbalans.app.ezbalans.databinding.ViewMainframeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ViewMainframeBinding

    private lateinit var currentFragment: Fragment
    private lateinit var fragmentProfile: FragmentProfile
    private lateinit var fragmentRooms: FragmentRooms
    private lateinit var fragmentWallet: FragmentWallet

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewMainframeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // check if deep linked to join room
        val uri = intent.data
        if (uri != null) {
            // if logged in move to join room fragment
            if (Firebase.auth.currentUser != null) {

                val joinFragmentRooms = FragmentRooms()
                val roomUid = uri.lastPathSegment!!

                val fragmentBundle = Bundle()
                fragmentBundle.putString(Constants.room_uid, roomUid)
                joinFragmentRooms.arguments = fragmentBundle
                setFragment(joinFragmentRooms, Constants.rooms_tag)
            }

            // if logged out move to login activity
            else {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        // init home activity
        else {
            fragmentRooms = FragmentRooms()
            fragmentWallet = FragmentWallet()
            fragmentProfile = FragmentProfile()

            setFragment(fragmentRooms, Constants.rooms_tag)
        }

        binding.bottomBar.onItemSelected = {
            when (it) {
                0 -> {
                    setFragment(fragmentRooms, Constants.rooms_tag)
                }
                1 -> {
                    setFragment(fragmentWallet, Constants.budgets_tag)
                }
                2 -> {
                    setFragment(fragmentProfile, Constants.profile_tag)
                }

            }
        }

    }


    private fun setFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainframe, fragment, tag)
            .setReorderingAllowed(true)
            .commit()
        currentFragment = fragment

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
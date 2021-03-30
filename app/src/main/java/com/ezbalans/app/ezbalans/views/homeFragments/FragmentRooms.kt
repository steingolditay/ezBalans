package com.ezbalans.app.ezbalans.views.homeFragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezbalans.app.ezbalans.adapters.MyRoomsAdapter
import com.ezbalans.app.ezbalans.helpers.Constants
import com.ezbalans.app.ezbalans.views.Notifications
import com.ezbalans.app.ezbalans.helpers.CreateNotification
import com.ezbalans.app.ezbalans.helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.R
import com.ezbalans.app.ezbalans.views.rooms.roomActivities.CreateRoom
import com.ezbalans.app.ezbalans.views.rooms.roomActivities.RoomActivity
import com.ezbalans.app.ezbalans.views.rooms.roomActivities.ShoppingList
import com.ezbalans.app.ezbalans.databinding.FragmentRoomsBinding
import com.ezbalans.app.ezbalans.helpers.GetPrefs
import com.ezbalans.app.ezbalans.viewmodels.homeFragments.RoomsFragmentViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.preference.PowerPreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentRooms: Fragment(), MyRoomsAdapter.OnItemClickListener {
    private var _binding: FragmentRoomsBinding? = null
    private val binding get() = _binding!!

    private val databaseReference = Firebase.database.reference
    val firebaseUser = Firebase.auth.currentUser!!
    lateinit var myRooms: List<Room>
    private val myRoomsKeys = arrayListOf<String>()
     lateinit var viewModel: RoomsFragmentViewModel

    private lateinit var adapter: MyRoomsAdapter

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRoomsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // deep-link join loading
        val roomUid = arguments?.getString(Constants.room_uid)
        if (roomUid != null){
            joinFromDeepLink(roomUid)
        }
        binding.notification.setOnClickListener{
            val intent = Intent(requireContext(), Notifications::class.java)
            startActivity(intent)
        }
        binding.badge.setOnClickListener{
            val intent = Intent(requireContext(), Notifications::class.java)
            startActivity(intent)
        }

        loadLanguageUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(RoomsFragmentViewModel::class.java)

        viewModel.getMyRooms().observe(requireActivity(), {
            initRecyclerView()
            myRooms = viewModel.getMyRooms().value!!
            updateRoomsVisibility(viewModel.getMyRooms().value!!.size)
        })

        viewModel.getMyNotifications().observe(requireActivity(), { notifications ->
            var counter = 0

            for (notification in notifications.values){
                if (!notification.seen){
                    counter +=1
                }
            }
            if (counter > 0){
                binding.badge.visibility = View.VISIBLE
                binding.badge.text = counter.toString()
            }
            else {
                binding.badge.visibility = View.GONE
                binding.badge.text = ""
            }
        })

    }

    private fun loadLanguageUI(){
        val lang = PowerPreference.getDefaultFile().getString(Constants.language)
        if (lang == Constants.language_hebrew){
            binding.fabHeb.visibility = View.VISIBLE
            binding.fab.visibility = View.GONE
            binding.arrow.setImageResource(R.drawable.arrow_heb)

            binding.joinRoomHeb.setOnClickListener{
                joinRoomDialog()
            }

            binding.createRoomHeb.setOnClickListener{
                if (!firebaseUser.isEmailVerified){
                    firebaseUser.getIdToken(true).addOnSuccessListener {
                        firebaseUser.reload().addOnSuccessListener {
                            if (firebaseUser.isEmailVerified){
                                val intent = Intent(context, CreateRoom::class.java)
                                startActivity(intent)
                                binding.fab.close(true)
                            }
                            else {
                                openNotVerifiedDialog()
                            }
                        }

                    }
                }
                else {
                    val intent = Intent(context, CreateRoom::class.java)
                    startActivity(intent)
                    binding.fab.close(true)
                }
            }
        }
        else {
            binding.fabHeb.visibility = View.GONE
            binding.fab.visibility = View.VISIBLE

            binding.joinRoom.setOnClickListener{
                joinRoomDialog()
            }

            binding.createRoom.setOnClickListener{
                firebaseUser.getIdToken(true).addOnSuccessListener {
                    firebaseUser.reload().addOnSuccessListener {
                        if (firebaseUser.isEmailVerified){
                            val intent = Intent(context, CreateRoom::class.java)
                            startActivity(intent)
                            binding.fab.close(true)
                        }
                        else {
                            openNotVerifiedDialog()
                        }
                    }
                }
            }
        }
    }

    private fun initRecyclerView(){
        adapter = MyRoomsAdapter(requireContext(), viewModel.getMyRooms().value, this)
        binding.list.layoutManager = LinearLayoutManager(context)
        binding.list.adapter = adapter

    }

    private fun openNotVerifiedDialog(){
        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_send_email_verification).create()
        val sendEmail = dialog.findViewById<Button>(R.id.send_email)

        sendEmail.setOnClickListener {
            firebaseUser.sendEmailVerification().addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(requireContext(), getString(R.string.verification_email_sent), Toast.LENGTH_SHORT).show()
                }
                else {
                    Log.d("TAG", "openNotVerifiedDialog: ${it.result}")
                }
                dialog.dismiss()
            }
        }
        dialog.show()

    }

    private fun joinRoomDialog(){
        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_join_room).create()
        val identityKey = dialog.findViewById<TextView>(R.id.identity)
        val sendRequest = dialog.findViewById<Button>(R.id.send_request)

        sendRequest.setOnClickListener {
            val roomIdentityKey = identityKey.text.toString()
            if (!myRoomsKeys.contains(roomIdentityKey)){
                requestJoinRoom(roomIdentityKey, dialog)

            }
            else {
                dialog.dismiss()
                Toast.makeText(requireContext(), getString(R.string.already_resident), Toast.LENGTH_SHORT).show()

            }
        }
        dialog.show()

        binding.fab.close(true)
    }

    private fun requestJoinRoom(identityKey: String, dialog: Dialog){
        val rooms = GetPrefs().getAllRooms()
        for (room in rooms.values){
            if (room.identity_key == identityKey){
                val status = room.residents[firebaseUser.uid]
                if (!room.residents.containsKey(firebaseUser.uid) || !(status == Constants.requested || status == Constants.added || status == Constants.declined)){
                    databaseReference.child(Constants.rooms).child(room.uid).child(Constants.residents).child(firebaseUser.uid).setValue(
                        Constants.requested).addOnSuccessListener {
                        CreateNotification().create(room, Constants.notify_user_requested, firebaseUser.uid, room.uid, "")
                        dialog.dismiss()
                        Toast.makeText(requireContext(),getString(R.string.request_sent), Toast.LENGTH_SHORT).show()

                    }


                }
                else {
                    Toast.makeText(requireContext(),getString(R.string.cant_send_request), Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (dialog.isShowing){
            dialog.dismiss()
            Toast.makeText(requireContext(),getString(R.string.cant_join_room), Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateRoomsVisibility(size: Int){
        if (size != 0){
            binding.emptyContainer.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
        }
        else {
            binding.emptyContainer.visibility = View.VISIBLE
            binding.list.visibility = View.GONE

        }

    }

    override fun onItemClick(position: Int) {
        val room = myRooms[position]
        val intent = Intent(context, RoomActivity::class.java)
        intent.putExtra(Constants.room_uid, room.uid)
        intent.putExtra(Constants.fragmentSelector, Constants.status_tag)
        startActivity(intent)
    }

    override fun onCartClick(position: Int) {
        val room = myRooms[position]
        val intent = Intent(context, ShoppingList::class.java)
        intent.putExtra(Constants.room_uid, room.uid)
        intent.putExtra(Constants.shopping_list, room.shopping_list)
        startActivity(intent)
    }

    override fun onDetailsClick(position: Int) {
        val room = myRooms[position]
        val intent = Intent(context, RoomActivity::class.java)
        intent.putExtra(Constants.room_uid, room.uid)
        intent.putExtra(Constants.fragmentSelector, Constants.details_tag)
        startActivity(intent)
    }

    override fun onEditClick(position: Int) {
        val room = myRooms[position]
        var currentBudget: Int = 0
        val dialog = GetCustomDialog(Dialog(requireContext()), R.layout.dialog_edit_room_budget).create()
        val budget = dialog.findViewById<EditText>(R.id.budget)
        val button = dialog.findViewById<Button>(R.id.apply)

        val myBudgets = GetPrefs().getMyBudgets()

        if (myBudgets.containsKey(room.uid)){
            val roomBudget = myBudgets[room.uid]!!
            budget.setText(roomBudget)
            currentBudget = roomBudget.toInt()
        }
        else {
            databaseReference.child(Constants.budgets).child(firebaseUser.uid).child(room.uid).setValue(0)
            budget.setText("0")

        }


        button.setOnClickListener {
            val newBudget = budget.text.toString().toInt()
            if (newBudget != currentBudget){
                databaseReference.child(Constants.budgets).child(firebaseUser.uid).child(room.uid).setValue(newBudget).addOnSuccessListener {
//                    getMyRooms()
                    dialog.dismiss()
                }
            }

        }

        dialog.show()

    }

    private fun joinFromDeepLink(roomUid: String){
        databaseReference.child(Constants.rooms).child(roomUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue<Room>()!!
                if (room.residents.containsKey(firebaseUser.uid)){
                    if (room.residents[firebaseUser.uid] == Constants.removed || room.residents[firebaseUser.uid] == Constants.declined){
                        Toast.makeText(requireContext(),getString(R.string.cant_join_room), Toast.LENGTH_SHORT).show()
//                        getMyRooms()
                    }

                    else if (room.residents[firebaseUser.uid] == Constants.added){
                        Toast.makeText(requireContext(),getString(R.string.already_resident), Toast.LENGTH_SHORT).show()
//                        getMyRooms()

                    }
                    else {
                        databaseReference.child(Constants.rooms).child(roomUid).child(Constants.residents).child(firebaseUser.uid).setValue(
                            Constants.added).addOnSuccessListener {
                            Toast.makeText(requireContext(),getString(R.string.you_joined_room), Toast.LENGTH_SHORT).show()
//                            getMyRooms()
                        }
                    }
                }
                else {
                    databaseReference.child(Constants.rooms).child(roomUid).child(Constants.residents).child(firebaseUser.uid).setValue(
                        Constants.added).addOnSuccessListener {
                        Toast.makeText(requireContext(),getString(R.string.you_joined_room), Toast.LENGTH_SHORT).show()
//                        getMyRooms()
                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}
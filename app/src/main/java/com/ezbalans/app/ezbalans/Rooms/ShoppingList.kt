package com.ezbalans.app.ezbalans.Rooms

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezbalans.app.ezbalans.Adapters.ShoppingListItemsAdapter
import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.Helpers.GetCustomDialog
import com.ezbalans.app.ezbalans.Models.Room
import com.ezbalans.app.ezbalans.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class ShoppingList: AppCompatActivity(), ShoppingListItemsAdapter.OnItemClickListener {

    var shoppingListUid = ""
    var roomUid = ""
    val databaseReference = Firebase.database.reference
    var room = Room()
    var shoppingList = arrayListOf<HashMap<String, Boolean>>()
    var items = arrayListOf<String>()
    lateinit var recyclerView: RecyclerView
    val adapter = ShoppingListItemsAdapter(this, shoppingList, this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_shopping_list)

        recyclerView = findViewById(R.id.list)

        val bundle = intent.extras
        if (bundle != null){
            if (bundle.containsKey(Constants.room_uid) && bundle.containsKey(Constants.shopping_list)){
                roomUid = bundle.getString(Constants.room_uid)!!
                shoppingListUid = bundle.getString(Constants.shopping_list)!!

                getRoom()
                getShoppingList()
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener{
            addItem()
        }
    }


    private fun getRoom(){
        databaseReference.child(Constants.rooms).child(roomUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                room = snapshot.getValue<Room>()!!
                val roomName = findViewById<TextView>(R.id.name)
                roomName.text = room.name

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getShoppingList(){
        shoppingList.clear()
        databaseReference.child(Constants.shopping_lists).child(shoppingListUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val key = item.key.toString()
                    val value = item.getValue<Boolean>()!!
                    val itemMap = HashMap<String, Boolean>()
                    itemMap[key] = value
                    shoppingList.add(itemMap)
                    items.add(key)
                }
                initAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initAdapter(){
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onItemNameClick(position: Int) {
        val itemName = shoppingList[position].keys.toTypedArray()[0]
        val itemValue = shoppingList[position].values.toTypedArray()[0]

        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_edit_item_name).create()
        val name = dialog.findViewById<EditText>(R.id.name)
        val button = dialog.findViewById<Button>(R.id.apply)
        name.setText(itemName)


        button.setOnClickListener {
            val newName = name.text.toString()
            databaseReference.child(Constants.shopping_lists).child(shoppingListUid).child(itemName).removeValue()
            databaseReference.child(Constants.shopping_lists).child(shoppingListUid).child(newName).setValue(itemValue)

            val newItem = HashMap<String, Boolean>()
            newItem[newName] = itemValue
            shoppingList[position] = newItem
            adapter.notifyItemChanged(position)
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onCheckBoxClick(position: Int, state: Boolean) {
        val itemName = shoppingList[position].keys.toTypedArray()[0]
        databaseReference.child(Constants.shopping_lists).child(shoppingListUid).child(itemName).setValue(state)
    }

    override fun onRemoveClick(position: Int) {
        val itemName = shoppingList[position].keys.toTypedArray()[0]

        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_remove_shopping_list_item).create()
        val title = dialog.findViewById<TextView>(R.id.title)
        val button = dialog.findViewById<Button>(R.id.remove_item)

        title.text = "'$itemName'"

        button.setOnClickListener {
            databaseReference.child(Constants.shopping_lists).child(shoppingListUid).child(itemName).removeValue()
            shoppingList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeRemoved(position, shoppingList.size)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addItem(){
        val dialog = GetCustomDialog(Dialog(this), R.layout.dialog_add_item).create()
        val name = dialog.findViewById<EditText>(R.id.name)
        val button = dialog.findViewById<Button>(R.id.apply)

        button.setOnClickListener {
            val itemName = name.text.toString()

            if (!items.contains(itemName)){
                databaseReference.child(Constants.shopping_lists).child(shoppingListUid).child(itemName).setValue(false)
                val newItem = HashMap<String, Boolean>()
                newItem[itemName] = false
                shoppingList.add(newItem)
                items.add(itemName)
                adapter.notifyDataSetChanged()
            }
            else{
                Toast.makeText(this, getString(R.string.item_exists), Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }
        dialog.show()
    }


}
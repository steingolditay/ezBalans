package com.ezbalans.app.ezbalans.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ezbalans.app.ezbalans.R
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ShoppingListItemsAdapter (private val items: ArrayList<HashMap<String, Boolean>>,
                                private val listener: OnItemClickListener) :
        RecyclerView.Adapter<ShoppingListItemsAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun onItemNameClick(position: Int)
        fun onCheckBoxClick(position: Int, state: Boolean)
        fun onRemoveClick(position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.name)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val remove: ImageView = itemView.findViewById(R.id.remove)

        init {
            itemView.setOnClickListener(this)

            name.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    listener.onItemNameClick(position)
                }
            }

            checkBox.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    listener.onCheckBoxClick(position, checkBox.isChecked)
                }
            }

            remove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    listener.onRemoveClick(position)
                }
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemNameClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val map = items[position]
        val itemName = map.keys.toTypedArray()[0]
        val itemState = map.values.toTypedArray()[0]

        holder.name.text = itemName
        holder.checkBox.isChecked = itemState

    }

    override fun getItemCount(): Int {
        return items.size
    }



}


package com.ezbalans.app.ezbalans.helpers

import com.ezbalans.app.ezbalans.Constants
import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Payment
import com.ezbalans.app.ezbalans.models.Room
import com.ezbalans.app.ezbalans.models.User
import com.preference.MapStructure
import com.preference.PowerPreference

class GetPrefs {

    fun getAllUsers(): HashMap<String, User>{
        val mapStructure = MapStructure.create(HashMap::class.java, String::class.java, User::class.java)
        return PowerPreference.getDefaultFile().getMap(Constants.users,mapStructure)!!
    }


    fun getNotifications(): HashMap<String, Notification>{
        val mapStructure = MapStructure.create(HashMap::class.java, String::class.java, Notification::class.java)
        return PowerPreference.getDefaultFile().getMap(Constants.notifications,mapStructure)!!
    }

    fun getAllRooms(): HashMap<String, Room>{
        val mapStructure = MapStructure.create(HashMap::class.java, String::class.java, Room::class.java)

        return PowerPreference.getDefaultFile().getMap(Constants.rooms, mapStructure)!!
    }

    fun getMyRooms(): HashMap<String, Room>{
        val mapStructure = MapStructure.create(HashMap::class.java, String::class.java, Room::class.java)
        return PowerPreference.getDefaultFile().getMap(Constants.my_rooms,mapStructure)!!
    }

    fun getAllPayments(): HashMap<String, Payment>{
        val mapStructure = MapStructure.create(HashMap::class.java, String::class.java, Payment::class.java)
        return PowerPreference.getDefaultFile().getMap(Constants.payments,mapStructure)!!
    }


    fun getMyPayments(): HashMap<String, Payment>{
        val mapStructure = MapStructure.create(HashMap::class.java, String::class.java, Payment::class.java)
        return PowerPreference.getDefaultFile().getMap(Constants.my_payments,mapStructure)!!
    }


    fun getMyShoppingList(): HashMap<String, HashMap<String, Boolean>>{
        val mapStructure = MapStructure.create(HashMap::class.java, String::class.java, HashMap::class.java)
        return PowerPreference.getDefaultFile().getMap(Constants.shopping_lists,mapStructure)!!
    }

    fun getMyBudgets(): HashMap<String, String> {
        val mapStructure = MapStructure.create(HashMap::class.java, String::class.java, String::class.java)
        return PowerPreference.getDefaultFile().getMap(Constants.budgets, mapStructure)!!
    }
}
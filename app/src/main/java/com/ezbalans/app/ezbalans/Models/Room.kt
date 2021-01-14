package com.ezbalans.app.ezbalans.Models

class Room (
        var uid: String = "",
        var identity_key: String = "",
        var name: String = "",
        var description: String = "",
        var image: String = "",
        var creation_date: String = "",
        var closing_date: String = "",
        var monthly_budget: String = "",
        var admins: HashMap<String, Boolean> = HashMap(),
        var residents: HashMap<String, String> = HashMap(),
        var shopping_list: String = "",
        var type: String = "",
        var currency: String = "",
        var categories: HashMap<String, Boolean> = HashMap(),
        var status: String = "",
        var motd: String = ""
)
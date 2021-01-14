package com.ezbalans.app.ezbalans.Models

class Notification (
        var uid: String = "",
        var room_uid: String = "",
        var type: String = "",
        var timestamp: Long = 0,
        var seen: Boolean = false,
        var source_uid: String = "",
        var target_uid: String = "",
        var extra: String = ""
)
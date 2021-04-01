package com.ezbalans.app.ezbalans.utils

import com.ezbalans.app.ezbalans.models.Notification
import com.ezbalans.app.ezbalans.models.Room
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CreateNotification {
    private val calendar = Calendar.getInstance(TimeZone.getDefault())
    private val databaseReference = Firebase.database.reference

    fun create(room: Room, type: String, source: String, target: String, extra: String){

        val uid = UUID.randomUUID().toString()
        val roomUid = room.uid
        val timestamp = calendar.time.time
        val seen = false

        val notification = Notification(uid, roomUid, type, timestamp, seen, source, target, extra)
        when (type){
            Constants.notify_user_joined -> {
                val posts = hashMapOf<String, Any>()
                posts["$target/$uid"] = notification

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)
            }

            Constants.notify_user_declined -> {
                val posts = hashMapOf<String, Any>()
                posts["$target/$uid"] = notification

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)
            }

            Constants.notify_user_quit -> {
                // source:  the user which quit
                // target:  ""
                // extra:   ""
                val posts = hashMapOf<String, Any>()

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)
            }
            // DONE
            Constants.notify_user_removed -> {
                // source:  the admin who removed the user
                // target:  the user which was removed
                // extra:   ""

                val posts = hashMapOf<String, Any>()
                posts["$target/$uid"] = notification

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }
            // DONE
            Constants.notify_user_requested -> {
                // source:  the user who requests to join
                // target:  the room which the user requests to join
                // extra:   ""
                val posts = hashMapOf<String, Any>()

                for (admin in room.admins){
                    if (admin.value){
                        posts["${admin.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }
            // DONE
            Constants.notify_room_info_changed -> {
                // source:  the admin made the change
                // target:  ""
                // extra:   ""
                val posts = hashMapOf<String, Any>()

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)
            }
            // DONE
            Constants.notify_payment_invalid -> {
                // source:  the user who created the invalid payment
                // target:  the payment
                // extra:   ""
                val posts = hashMapOf<String, Any>()

                for (admin in room.admins){
                    if (admin.value){
                        posts["${admin.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }
            // DONE
            Constants.notify_payment_validated -> {
                // source:  the admin who validated the payment
                // target:  the user who created the invalid payment
                // extra:   the payment

                val posts = hashMapOf<String, Any>()
                posts["$target/$uid"] = notification

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }
            // DONE
            Constants.notify_payment_declined -> {
                // source:  the admin who declined the payment
                // target:  the user who created the invalid payment
                // extra:   the payment

                val posts = hashMapOf<String, Any>()
                posts["$target/$uid"] = notification

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }

            // DONE
            Constants.notify_room_closed -> {
                // source:  the admin who closed the room
                // target:  ""
                // extra:   ""

                val posts = hashMapOf<String, Any>()

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }
            // DONE
            Constants.notify_admin_demoted -> {
                // source:  the admin who demoted the admin
                // target:  the admin who got demoted
                // extra:   ""

                val posts = hashMapOf<String, Any>()
                posts["$target/$uid"] = notification

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }
            // DONE
            Constants.notify_admin_promoted -> {
                // source:  the admin who promoted the resident
                // target:  the resident who got promoted
                // extra:   ""

                val posts = hashMapOf<String, Any>()
                posts["$target/$uid"] = notification

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }

            Constants.notify_motd_changed -> {
                // source:  the resident who made the change
                // target:  ""
                // extra:   the new motd text

                val posts = hashMapOf<String, Any>()

                for (resident in room.residents){
                    if (resident.value == Constants.added){
                        posts["${resident.key}/$uid"] = notification
                    }
                }

                databaseReference.child(Constants.notifications).updateChildren(posts)

            }


        }



    }

}
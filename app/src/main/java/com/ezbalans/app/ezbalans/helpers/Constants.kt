package com.ezbalans.app.ezbalans.helpers

object Constants {


    // general
    const val tag = "TAG";
    const val admin = "admin"
    const val main_activity_count = "main_activity_count"
    const val room_activity_count = "room_activity_count"
    const val room_uid = "room_uid"
    const val language = "language"
    const val language_english = "english"
    const val language_hebrew = "hebrew"
    const val motd = "motd"
    const val base_join_url = "http://ezbalans.com/join/"
    const val email = "email"
    const val password = "password"
    const val my_rooms = "my_rooms"
    const val my_payments = "my_payments"
    const val first_time = "first_time"


    // db
    const val rooms = "rooms"
    const val room = "room"
    const val payments = "payments"
    const val users = "users"
    const val image = "image"
    const val residents = "residents"
    const val admins = "admins"
    const val budgets = "budgets"
    const val description = "description"
    const val monthly_budget = "monthly_budget"
    const val type = "type"
    const val currency = "currency"
    const val categories = "categories"
    const val name = "name"
    const val uid = "uid"
    const val shopping_lists = "shopping_lists"
    const val break_even = "break_even"
    const val shopping_list = "shopping_list"
    const val time_stamp = "time_stamp"
    const val first_name = "first_name"
    const val last_name = "last_name"
    const val username = "username"
    const val notifications = "notifications"

    // notifications
    const val notify_user_joined = "user_joined"
    const val notify_user_declined = "user_declined"
    const val notify_user_removed = "user_removed"
    const val notify_user_quit = "user_quit"
    const val notify_user_requested = "user_requested"
    const val notify_room_info_changed = "room_info_changed"
    const val notify_payment_validated = "payment_validated"
    const val notify_payment_invalid = "payment_invalid"
    const val notify_payment_declined = "payment_declined"
    const val notify_room_closed = "notify_room_closed"
    const val notify_admin_demoted = "notify_admin_demoted"
    const val notify_admin_promoted = "notify_admin_promoted"
    const val notify_motd_changed = "notify_motd_changed"
    const val seen = "seen"

    // payment statuses
    const val payment_valid = "valid"
    const val payment_invalid = "invalid"
    const val payment_declined = "declined"

    // room statuses
    const val room_active = "active"
    const val room_inactive = "inactive"
    const val room_closing_date = "closing_date"
    const val status = "status"

    // resident statuses
    const val added = "added"
    const val requested = "requested"
    const val quit = "quit"
    const val removed = "removed"
    const val declined = "declined"

    const val IMAGE_CROP_REQUEST_CODE = 1111

    // fragment tags
    const val fragmentSelector = "FRAGMENT_SELECTOR"
    const val profile_tag = "PROFILE_TAG"
    const val rooms_tag = "ROOMS_TAG"
    const val budgets_tag = "BUDGETS_TAG"
    const val past_tag = "PAST_TAG"
    const val status_tag = "STATUS_TAG"
    const val details_tag = "DETAILS_TAG"
    const val history_tag = "HISTORY_TAG"

    //currencies
    @JvmField val room_currencies = arrayListOf<String>("USD", "NIS")
    const val nis = "NIS"
    const val usd = "USD"
    const val nis_symbol = "₪"
    const val usd_symbol = "$"

    // room types
    const val category_rent = "Rent"
    const val category_bills = "Bills"
    const val category_repairs = "Repairs"
    const val category_groceries = "Groceries"
    const val category_clothing = "Clothing"
    const val category_medical = "Medical and Healthcare"
    const val category_gifts = "Gifts"
    const val category_entertainment = "Entertainment"
    const val category_alcohol = "Alcohol"
    const val category_tobacco = "Tobacco and Smoking"
    const val category_gas = "Gas"
    const val category_car = "Car Maintenance"
    const val category_parking = "Parking"
    const val category_pet = "Pet Food"
    const val category_phone = "Phone"
    const val category_internet = "Internet"
    const val category_tv = "TV and Streaming"
    const val category_insurance = "Insurance"
    const val category_gym = "Gym"
    const val category_haircut = "Haircut and Salon"
    const val category_babysitter = "Babysitter"
    const val category_loans = "Loans"
    const val category_education = "Education"
    const val category_school = "School"
    const val category_vacations = "Vacations"
    const val category_other = "Other"



    const val default_user_image = "https://firebasestorage.googleapis.com/v0/b/ezbalans-87bc5.appspot.com/o/user.png?alt=media&token=34b4b9da-062a-4822-826f-05aabb08dc74"
    const val default_room_image = "https://firebasestorage.googleapis.com/v0/b/ezbalans-87bc5.appspot.com/o/open-door.png?alt=media&token=ef394a81-7bcf-4b1d-8880-06b728b18aa4"

    @JvmField val room_types = arrayListOf<String>("Family", "Roommates", "Couple", "Vacation")


    @JvmField val room_category_family = arrayListOf<String>(
        category_rent, category_bills, category_repairs, category_groceries, category_clothing, category_medical,
                                                                     category_gifts, category_entertainment, category_alcohol, category_tobacco, category_gas, category_car, category_parking, category_pet,
                                                                     category_phone, category_internet, category_tv, category_insurance, category_gym, category_haircut, category_babysitter, category_loans,
                                                                     category_education, category_school, category_vacations, category_other
    )

    val room_category_roommates = arrayListOf<String>(
        category_rent, category_bills, category_repairs, category_groceries, category_pet, category_internet,
                                                                    category_tv, category_other
    )

    val room_category_couple = arrayListOf<String>(
        category_rent, category_bills, category_repairs, category_groceries, category_medical, category_gifts, category_entertainment, category_alcohol,
                                                                    category_tobacco, category_gas, category_car, category_parking, category_pet, category_phone, category_internet, category_tv, category_loans,
                                                                    category_vacations, category_other
    )

    val room_category_vacation = arrayListOf<String>(
        category_rent, category_groceries, category_medical, category_entertainment, category_alcohol, category_tobacco, category_gas,
                                                                    category_insurance, category_loans, category_other
    )



}
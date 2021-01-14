package com.ezbalans.app.ezbalans.Helpers

class TranslateToHebrew {


    fun paymentCategory(input: String): String{
        when (input){
            "Rent" -> {
                return "שכר דירה"
            }
            "Bills" -> {
                return "חשבונות"

            }
            "Repairs" -> {
                return "תיקונים"

            }
            "Groceries" -> {
                return "קניות"

            }
            "Clothing" -> {
                return "ביגוד"

            }
            "Medical and Healthcare" -> {
                return "רפואה ותרופות"

            }
            "Gifts" -> {
                return "מתנות"

            }
            "Entertainment" -> {
                return "בידור ופנאי"

            }
            "Alcohol" -> {
                return "אלכוהול"

            }
            "Tobacco and Smoking" -> {
                return "טבק ומוצרי עישון"

            }
            "Gas" -> {
                return "דלק"

            }
            "Car Maintenance" -> {
                return "תחזוקת רכב"

            }
            "Parking" -> {
                return "חניה"

            }
            "Pet Food" -> {
                return "אוכל לחיות"

            }
            "Phone" -> {
                return "טלפון"

            }
            "Internet" -> {
                return "אינטרנט"

            }
            "TV and Streaming" -> {
                return "טלוויזיה וסטרימינג"

            }
            "Insurance" -> {
                return "ביטוח"

            }
            "Gym" -> {
                return "חדר כושר"

            }
            "Haircut and Salon" -> {
                return "תספורת ומכון יופי"

            }
            "Babysitter" -> {
                return "בייביסיטר"

            }

            "Loans" -> {
                return "הלוואות"

            }
            "Education" -> {
                return "לימודים"

            }
            "School" -> {
                return "בית ספר"

            }
            "Vacations" -> {
                return "חופשה"

            }
            "Other" -> {
                return "אחר"

            }
            else -> {
                return "אחר"
            }

        }
    }

    fun roomType(type: String): String {
        when (type){
            "Family" -> {
                return "משפחה"
            }
            "Roommates" -> {
                return "שותפים"
            }
            "Couple" -> {
                return "זוג"
            }
            "Vacation" -> {
                return "חופשה"
            }
            else -> {
                return ""
            }
        }

    }

    fun roomCurrency(type: String): String {
        when (type){
            "NIS" -> {
                return "שקל"
            }
            "USD" -> {
                return "דולר"
            }
            else -> {
                return ""
            }
        }

    }
}

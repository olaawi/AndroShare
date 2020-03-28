package com.example.androshare

class User constructor(
    var givenName: String,
    var familyName: String,
    var email: String,
    var id: String
) {
    var avatar: Int = R.drawable.avatar1 // default avatar
    var events: ArrayList<String> = ArrayList()
}
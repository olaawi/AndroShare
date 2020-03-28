package com.example.androshare

import android.net.Uri
import java.util.*

class Image(var uri: Uri) {

    var drawable: Int = R.drawable.avatar3
    var id: String = UUID.randomUUID().toString()

    var isSelected: Boolean = false

    fun toggleSelect() {
        isSelected = !isSelected
    }

    fun unselect() {
        isSelected = false
    }
}
package com.example.androshare

class Image {

    var drawable : Int = R.drawable.avatar3
    var isSelected : Boolean = false

    fun toggleSelect(){
        isSelected = !isSelected
    }

    fun unselect(){
        isSelected = false
    }
}
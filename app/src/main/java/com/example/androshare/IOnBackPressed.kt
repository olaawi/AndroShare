package com.example.androshare

interface IOnBackPressed {

    /**
     * return true for custom back behaviour, false for default
     */
    fun customOnBackPressed() : Boolean
}
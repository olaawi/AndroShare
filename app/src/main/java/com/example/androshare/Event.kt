package com.example.androshare

import android.location.Location
import android.sax.StartElementListener
import android.util.Log
import com.google.android.libraries.places.api.model.Place
import java.io.FileDescriptor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class Event(
    title: String,
    description: String,
    creator: User,
    type: EventType,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    location: EventLocation,
    pin: String
    ) {

    var title: String = title
    var description: String = description
    var creator: User = creator
    var type: EventType = type
    var startTime: LocalDateTime = startTime
    var endTime: LocalDateTime = endTime
    var location: EventLocation = location
    var id: String = UUID.randomUUID().toString()
    val admins = mutableListOf<User>()
    val participants = mutableListOf<User>()
    val pin = pin

    init {
//        idGenerator++
//        this.id = getNewId()
    }

//    constructor(
//        title: String,
//        description: String,
//        creator: User,
//        type: EventType,
//        startTime: LocalDateTime,
//        endTime: LocalDateTime,
//        location: EventLocation,
//        pin: String,
//        id: String
//    ) : this(title, description, creator, type, startTime, endTime, location, pin) {
//        this.id = id
//        Log.d("Event-Constructor", "id= " + this.id)
//    }

//    companion object {
//        var idGenerator: Int = 0;
//
//        fun getNewId(): Int {
//            return idGenerator
//        }
//    }

    enum class EventType {
        PUBLIC_EVENT, PRIVATE_EVENT
    }

    fun getTime(): String {
        val startTimeFormatted =
            startTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        val endTimeFormatted = endTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        return "$startTimeFormatted - $endTimeFormatted"
    }

    init {
        admins.add(creator)
        participants.add(creator)
    }

    fun addAdmin(newAdmin: User) {
        admins.add(newAdmin)
        if (!participants.contains(newAdmin))
            participants.add(newAdmin)
    }

    fun isAdmin(user: User): Boolean {
        return admins.contains(user)
    }

    fun addParticipant(newParticipant: User) {
        participants.add(newParticipant)
    }

    fun isParticipant(user: User): Boolean {
        return participants.contains(user)
    }

}


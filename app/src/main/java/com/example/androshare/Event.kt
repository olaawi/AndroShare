package com.example.androshare

import android.location.Location
import android.sax.StartElementListener
import com.google.android.libraries.places.api.model.Place
import java.io.FileDescriptor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Event(
    title: String,
    description: String,
    creator: User,
    type: EventType,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    location: EventLocation
    ) {

    var title: String = title
    var description: String = description
    var creator: User = creator
    var type: EventType = type
    var startTime: LocalDateTime = startTime
    var endTime: LocalDateTime = endTime
    var location: EventLocation = location
    var id: Int = 0
    val admins = mutableListOf<User>()
    val participants = mutableListOf<User>()

    init {
        idGenerator++
        this.id = getNewId()
    }

    constructor(
        title: String,
        description: String,
        creator: User,
        type: EventType,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        location: EventLocation,
        id: Int
    ) : this(title, description, creator, type, startTime, endTime, location) {
        this.id = id
    }

    companion object {
        var idGenerator: Int = 0;

        fun getNewId(): Int {
            return idGenerator
        }
    }

    public enum class EventType {
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

    public fun addAdmin(newAdmin: User) {
        admins.add(newAdmin)
        if (!participants.contains(newAdmin))
            participants.add(newAdmin)
    }

    public fun isAdmin(user: User): Boolean {
        return admins.contains(user)
    }

    public fun addParticipant(newParticipant: User) {
        participants.add(newParticipant)
    }

    public fun isParticipant(user: User): Boolean {
        return participants.contains(user)
    }

}


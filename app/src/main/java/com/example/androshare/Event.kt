package com.example.androshare

import java.io.FileDescriptor

class Event(title : String, description: String, creator: User, type: EventType) {

    public var title : String = title
    public var description : String = description
    public var creator: User = creator

    public var type: EventType = type
        set(value) {
            require(value == EventType.PUBLIC_EVENT || value == EventType.PRIVATE_EVENT) { "Event must be public or private" }
            field = value
        }

    public val admins = mutableListOf<User>()
    public val participants = mutableListOf<User>()

    public enum class EventType {
        PUBLIC_EVENT, PRIVATE_EVENT
    }

    init {
        admins.add(creator)
        participants.add(creator)
    }

    public fun addAdmin(newAdmin: User) {
        admins.add(newAdmin)
        if(!participants.contains(newAdmin))
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


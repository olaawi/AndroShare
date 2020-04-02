package com.example.androshare

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class Event(
    var title: String,
    var description: String,
    var creator: User,
    var type: EventType,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var location: EventLocation,
    val pin: String
) {

    var id: String = UUID.randomUUID().toString()
    val admins = mutableListOf<String>()
    val participants = mutableListOf<String>()

    init {
    }

    enum class EventType {
        PUBLIC_EVENT, PRIVATE_EVENT
    }

    fun getTime(): String {
        val startTimeFormatted =
            startTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        val endTimeFormatted = endTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        return "$startTimeFormatted - $endTimeFormatted"
    }

    fun getDate(): String {
        val startDateFormatted =
            startTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        val endDateFormatted = endTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        return "$startDateFormatted - $endDateFormatted"
    }

    init {
        admins.add(creator.id)
        participants.add(creator.id)
    }

    fun addAdmin(newAdmin: String) {
        admins.add(newAdmin)
        if (!participants.contains(newAdmin))
            participants.add(newAdmin)
    }

    fun isAdmin(user: String): Boolean {
        return admins.contains(user)
    }

    fun addParticipant(newParticipant: String) {
        participants.add(newParticipant)
    }

    fun isParticipant(user: String): Boolean {
        return participants.contains(user)
    }

}


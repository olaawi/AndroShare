package com.example.androshare

import EventAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime


class Dashboard : Fragment() {

    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private var events: ArrayList<Event?> = ArrayList()
    private lateinit var newEventButton: View
    private lateinit var eventPageFragment: EventPage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseFirestore.getInstance()


        /*
        this.events = arrayListOf()
        this.events.add(
            Event(
                "Event 1", "this is my first event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 1", 0.0, 0.0),
                "1111"
            )
        )

        this.events.add(
            Event(
                "Event 2", "this is my second event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 2", 0.0, 0.0),
                "2222"
            )
        )

        this.events.add(
            Event(
                "Event 3", "this is my second event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 3", 0.0, 0.0),
                "3333"
            )
        )

        this.events.add(
            Event(
                "Event 4", "this is my second event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 4", 0.0, 0.0),
                "4444"
            )
        )

        this.events.add(
            Event(
                "Event 5", "this is my second event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 5", 0.0, 0.0),
                "5555"
            )
        )

        this.events.add(
            Event(
                "Event 6", "this is my second event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 6", 0.0, 0.0),
                "6666"
            )
        )

        this.events.add(
            Event(
                "Event 7", "this is my second event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 7", 0.0, 0.0),
                "7777"
            )
        )

        this.events.add(
            Event(
                "Event 8", "this is my second event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 8", 0.0, 0.0),
                "8888"
            )
        )

        val user1 = User("name", "family", "email", "1")
        val user2 = User("name", "family", "email", "2")
        val user3 = User("name", "family", "email", "3")
        val user4 = User("name", "family", "email", "4")
        user1.avatar = R.drawable.avatar3
        user2.avatar = R.drawable.avatar4
        user3.avatar = R.drawable.avatar5
        user4.avatar = R.drawable.avatar6
        events[0]!!.addParticipant(user1)
        events[0]!!.addParticipant(user2)
        events[0]!!.addParticipant(user3)
        events[0]!!.addParticipant(user4)
        events[0]!!.addParticipant(user4)
        events[0]!!.addParticipant(user4)
        events[0]!!.addParticipant(user4)
        events[0]!!.addParticipant(user4)
        events[0]!!.addParticipant(user4)
        events[0]!!.addParticipant(user4)
        events[0]!!.addParticipant(user4)
        */

    }

    private fun onEventClicked(event: Event) {
        eventPageFragment = EventPage(event)
        val transaction = fragmentManager!!.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction
            .add(android.R.id.content, eventPageFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.recyclerView = view.findViewById(R.id.recyclerView)
        this.recyclerView.layoutManager = LinearLayoutManager(this.context)
        this.eventAdapter =
            EventAdapter(this.context!!, this.events) { event: Event -> onEventClicked(event) }
        this.recyclerView.adapter = this.eventAdapter
        this.newEventButton = view.findViewById(R.id.new_event_button)

        this.newEventButton.setOnClickListener {
            val newFragment = NewEvent()
            val transaction = fragmentManager!!.beginTransaction()
            // TODO For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
        }

        // Create list of the user's events
        // Get current logged in user
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val user = User(account!!.givenName!!, account.familyName!!, account.email!!, account.id!!)
        val userDoc = database.collection("users").document(user.id)
        userDoc.get()
            .addOnSuccessListener { document ->
                val eventIds = document.get("events") as ArrayList<String>

                for (eventId in eventIds) {

                    database.collection("events").document(eventId).get()
                        .addOnSuccessListener { document ->

                            Log.e("Dash id = ", eventId)

                            var eventType: Event.EventType = Event.EventType.PUBLIC_EVENT
                            var pin = "0000"
                            if (document.get("type")!! == "PRIVATE_EVENT") {
                                eventType = Event.EventType.PRIVATE_EVENT
                                pin = document.get("pin")!! as String
                            }
                            val startTime = LocalDateTime.of(
                                (document.get("startTime.year")!! as Long).toInt(),
                                (document.get("startTime.monthValue")!! as Long).toInt(),
                                (document.get("startTime.dayOfMonth")!! as Long).toInt(),
                                (document.get("startTime.hour")!! as Long).toInt(),
                                (document.get("startTime.minute")!! as Long).toInt()
                            )
                            val endTime = LocalDateTime.of(
                                (document.get("endTime.year")!! as Long).toInt(),
                                (document.get("endTime.monthValue")!! as Long).toInt(),
                                (document.get("endTime.dayOfMonth")!! as Long).toInt(),
                                (document.get("endTime.hour")!! as Long).toInt(),
                                (document.get("endTime.minute")!! as Long).toInt()
                            )
                            val currentEvent = Event(
                                (document.get("title") as String?)!!,
                                (document.get("description") as String?)!!,
                                User(
                                    document.get("creator.givenName") as String,
                                    document.get("creator.familyName") as String,
                                    document.get("creator.email") as String,
                                    document.get("creator.id") as String
                                ),
                                eventType,
                                startTime,
                                endTime,
                                EventLocation(
                                    document.get("location.name")!! as String,
                                    document.get("location.latitude")!! as Double,
                                    document.get("location.longitude")!! as Double
                                ),
                                pin
                            )
                            currentEvent.id = document.get("id")!! as String
                            currentEvent.participants.clear() // Annoying bug
                            val usersHash = document.get("participants") as ArrayList<*>

                            for (userHash in usersHash) {
                                val avatar = ((userHash as HashMap<*, *>)["avatar"] as Long).toInt()
                                val givenName = (userHash as HashMap<*, *>)["givenName"] as String
                                val familyName = (userHash as HashMap<*, *>)["familyName"] as String
                                var user = User(givenName, familyName, "", "")
                                user.avatar = avatar
                                currentEvent.participants.add(user)
                                Log.d("Dash","added user " + user.givenName)
                            }

                            events.add(currentEvent)
                            eventAdapter.notifyItemInserted(events.size - 1)

                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Dashboard", "Failed to get user from database", exception)
            }

    }

}

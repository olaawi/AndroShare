package com.example.androshare

import EventAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val events: Array<Event?> = arrayOfNulls<Event>(2)
        events.set(0, Event("first event", "this is my first event",
            User("Ola", "Awisat", "ola@gmail", "0"),
            Event.EventType.PUBLIC_EVENT))

        events.set(1, Event("second event", "this is my second event",
            User("Ola", "Awisat", "ola@gmail", "0"),
            Event.EventType.PUBLIC_EVENT))

        eventAdapter = EventAdapter(this, events)
        recyclerView.adapter = eventAdapter


    }



}

package com.example.androshare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment

class ParticipantsList(private val event: Event) : Fragment() {

    private lateinit var participantsAdapter: ParticipantsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_participants_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val participantsList: ArrayList<User> = ArrayList(event.participants)
        participantsAdapter = ParticipantsAdapter(activity!!, participantsList)
        view.findViewById<ListView>(R.id.participants_list).adapter = participantsAdapter

        view.findViewById<ImageView>(R.id.participants_list_back).setOnClickListener {
            activity!!.onBackPressed()
        }
    }

}

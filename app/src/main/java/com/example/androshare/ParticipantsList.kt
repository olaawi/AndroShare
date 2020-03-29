package com.example.androshare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

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
        val database = FirebaseFirestore.getInstance()
        var participantsCount = 0
        val participantsList: ArrayList<User> = ArrayList()
        val participantsIds: ArrayList<String> = ArrayList(event.participants)
        for (currentParticipant in participantsIds) {
            database.collection("users").document(currentParticipant)
                .get()
                .addOnSuccessListener { document ->
                    participantsCount++
                    participantsList.add(
                        User(
                            document.get("givenName") as String,
                            document.get("familyName") as String,
                            document.get("email") as String,
                            document.get("id") as String
                        )
                    )
                    if (participantsCount == event.participants.size) {
                        participantsAdapter = ParticipantsAdapter(activity!!, participantsList)
                        view.findViewById<ListView>(R.id.participants_list).adapter =
                            participantsAdapter
                    }
                }
        }
        view.findViewById<ImageView>(R.id.participants_list_back).setOnClickListener {
            activity!!.onBackPressed()
        }
    }

}

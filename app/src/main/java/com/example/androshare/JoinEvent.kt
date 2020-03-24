package com.example.androshare

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore


class JoinEvent(private val event: Event) : DialogFragment() {

    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var pinTextView: TextView
    private lateinit var pinEditText: TextView
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button
    private lateinit var database: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_event, container, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleTextView = view.findViewById(R.id.join_event_title) as TextView
        descriptionTextView = view.findViewById(R.id.join_event_description) as TextView
        timeTextView = view.findViewById(R.id.join_event_time) as TextView
        locationTextView = view.findViewById(R.id.join_event_location) as TextView
        pinTextView = view.findViewById(R.id.join_event_enter_pin) as TextView
        pinEditText = view.findViewById(R.id.join_event_pin) as EditText
        confirmButton = view.findViewById(R.id.join_event_join_button) as Button
        cancelButton = view.findViewById(R.id.join_event_cancel_button) as Button

        titleTextView.text = event.title
        descriptionTextView.text = event.description
        timeTextView.text = event.getTime()
        locationTextView.text = event.location.name
        if (event.type == Event.EventType.PRIVATE_EVENT) {
            pinTextView.visibility = View.VISIBLE
            pinEditText.visibility = View.VISIBLE
        }

        confirmButton.setOnClickListener {
            if (event.type == Event.EventType.PRIVATE_EVENT && event.pin != pinEditText.text) {
                Snackbar.make(view, "Wrong PIN, try again!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
            } else {
                //TODO add event for user + add user as participant
                val account = GoogleSignIn.getLastSignedInAccount(context)
                val user = User(
                    account!!.givenName!!,
                    account.familyName!!,
                    account.email!!,
                    account.id!!
                )
                // add event to user
                val doc1 = database.collection("users").document(account.id!!)
                doc1.get()
                    .addOnSuccessListener { document ->
                        val eventsList = document.get("events") as ArrayList<String>
                        eventsList.add(event.id)
                        database.collection("users").document(account.id!!)
                            .update("events", eventsList)
                            .addOnSuccessListener {
                                Log.d("JoinEvent", "Added event to user")
                            }
                            .addOnFailureListener {
                                Log.e("JoinEvent", "Error adding event to user")
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("JoinEvent", "Error adding event to user", exception)
                    }
                // add user as participant
                val doc = database.collection("events").document(event.id)
                doc.get()
                    .addOnSuccessListener { document ->
                        val usersList = document.get("participants") as ArrayList<User>
                        usersList.add(user)
                        database.collection("events").document(event.id)
                            .update("participants", usersList)
                            .addOnSuccessListener {
                                Log.d("JoinEvent", "Joined event with ID: ${document.id}")
                                this.dismiss()
                                Snackbar.make(
                                    view,
                                    "Successfully joined event!",
                                    Snackbar.LENGTH_LONG
                                )
                                    .setAction("Action", null)
                                    .show()
                            }
                            .addOnFailureListener {
                                Log.e("JoinEvent", "Error joining event")
                                this.dismiss()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("JoinEvent", "Error joining event", exception)
                        this.dismiss()
                    }
            }
        }
        cancelButton.setOnClickListener {
            this.dismiss()
        }
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        listener = null
//    }

//    interface OnFragmentInteractionListener {
//        fun onFragmentInteraction(uri: Uri)
//    }

//companion object {
//}
}

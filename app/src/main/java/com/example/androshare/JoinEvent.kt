package com.example.androshare

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
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
            //            if (event.pin != pinEditText.text) {
//                Snackbar.make(view, "Wrong PIN, try again!", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null)
//                    .show()
//            } else {
//                //TODO add event for user + add user as participant
//                database.collection("events").get(event)
//                    .addOnSuccessListener { documentReference ->
//                        Log.d(
//                            "newEvent",
//                            "Event added with ID: ${documentReference.id}"
//                        )
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.w("newEvent", "Error adding event", exception)
//                    }
//
            this.dismiss()
            Snackbar.make(view, "Successfully joined event!", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
//            }

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

//companion object {
//}
}

package com.example.androshare

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.new_event_dialog.view.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import EventAdapter
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import java.util.*
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Dashboard : Fragment(),PlaceSelectionListener {

    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var eventLocation: Place
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var events: Array<Event?>
    private lateinit var newEventButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.param1 = it.getString(ARG_PARAM1)
            this.param2 = it.getString(ARG_PARAM2)
        }

        // initialize places + DB
        Places.initialize(this!!.context!!, this.getString(R.string.places_api_key))
        database = FirebaseFirestore.getInstance()

        // create list of the user's event_in_dashboard
        this.events = arrayOfNulls<Event>(2)
        this.events[0] = Event(
            "first event_in_dashboard", "this is my first event_in_dashboard",
            User("Ola", "Awisat", "ola@gmail", "0"),
            Event.EventType.PUBLIC_EVENT
        )

        this.events[1] = Event(
            "second event_in_dashboard", "this is my second event_in_dashboard",
            User("Ola", "Awisat", "ola@gmail", "0"),
            Event.EventType.PUBLIC_EVENT
        )
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
        this.eventAdapter = EventAdapter(this.context!!, this.events)
        this.recyclerView.adapter = this.eventAdapter
        this.newEventButton = view.findViewById(R.id.new_event_button)
        // When new event_in_dashboard button is clicked:
        this.newEventButton.setOnClickListener {
            //Inflate the dialog with custom view
            val newEventDialogView = LayoutInflater.from(this.activity).inflate(R.layout.new_event_dialog, null)
            //AlertDialogBuilder
            val dialogBuilder = AlertDialog.Builder(this.activity!!)
                .setView(newEventDialogView)
                .setTitle("Create New Event")

            // autocomplete location: place return fields
            val locationAutocompleteFragment =
                this.fragmentManager?.findFragmentById(R.id.event_location_autocomplete) as AutocompleteSupportFragment
            locationAutocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))
            locationAutocompleteFragment.setOnPlaceSelectedListener(this)

            //show dialog
            val mAlertDialog = dialogBuilder.show()

            //confirm new event
            newEventDialogView.confirm_button.setOnClickListener {
                val eventNameEditText = view.findViewById(R.id.new_event_name) as EditText
                val eventName = eventNameEditText.getText().toString()
                var eventType = Event.EventType.PUBLIC_EVENT
                val evenTypeCheckBox = view.findViewById(R.id.event_type_check_box) as CheckBox
                if(evenTypeCheckBox.isChecked){
                    eventType = Event.EventType.PRIVATE_EVENT
                }
                val eventCreator = User("Hala", "Awisat", "email@gmail.com", "1234567890")
                val event = Event(eventName, "WHY?", eventCreator, eventType)

              //TODO: add event to DB + add event for user (admin)

                database.collection("events").add(event)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            "newEvent",
                            "Event added with ID: ${documentReference.id}"
                        )
                    }
                    .addOnFailureListener { exception ->
                        Log.w("newEvent", "Error adding event", exception)
                    }

                mAlertDialog.dismiss()
                Snackbar.make(view, "Event created successfully!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
            }
            //cancel new event
            newEventDialogView.cancel_button.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
    }

    override fun onPlaceSelected(place: Place) {
//        Toast.makeText(context,""+p0.name+p0.latLng, Toast.LENGTH_LONG).show()
        this.eventLocation = place
    }

    override fun onError(status: Status) {
        Toast.makeText(this.context,""+status.toString(),Toast.LENGTH_LONG).show()
    }



    fun onButtonPressed(uri: Uri) {
        this.listener?.onFragmentInteraction(uri)
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }

    override fun onDetach() {
        super.onDetach()
        this.listener = null
    }

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Dashboard.
         */

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Dashboard().apply {
                this.arguments = Bundle().apply {
                    this.putString(ARG_PARAM1, param1)
                    this.putString(ARG_PARAM2, param2)
                }
            }
    }
}

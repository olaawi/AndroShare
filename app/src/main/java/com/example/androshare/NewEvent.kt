package com.example.androshare

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.new_event_dialog.*
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [NewEvent.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [NewEvent.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewEvent : DialogFragment(), PlaceSelectionListener {

    private lateinit var database: FirebaseFirestore
    private lateinit var eventLocation: Place

//    private var param1: String? = null
//    private var param2: String? = null
//    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
//        setStyle(STYLE_NORMAL,R.style.FullScreenDialogStyle)
        // initialize places + DB
        if (!Places.isInitialized()) {
            Places.initialize(context!!, getString(R.string.places_api_key))
        }
        database = FirebaseFirestore.getInstance()
    }

    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment
        return inflater.inflate(R.layout.new_event_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // TODO the following causes null pointer exception fix it
        // autocomplete location: place return fields
//        val locationAutocompleteFragment =
//            fragmentManager!!.findFragmentById(R.id.event_location_autocomplete) as AutocompleteSupportFragment
//        locationAutocompleteFragment.setPlaceFields(
//            listOf(
//                Place.Field.ID,
//                Place.Field.NAME
//            )
//        )
//        locationAutocompleteFragment.setOnPlaceSelectedListener(this)


        // show start date picker dialog
        val c = Calendar.getInstance()
        val currYear = c.get(Calendar.YEAR)
        val currMonth = c.get(Calendar.MONTH)
        val currMonthNormalized = c.get(Calendar.MONTH) + 1 // months are indexed starting at 0
        val currDayOfMonth = c.get(Calendar.DAY_OF_MONTH)
        chosen_start_date.text = "$currDayOfMonth/$currMonthNormalized/$currYear"
        start_date.setOnClickListener {
            val dpd = DatePickerDialog(
                this.activity!!,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val chosenMonth = month + 1 // months are indexed starting at 0
                    chosen_start_date.text = "$dayOfMonth/$chosenMonth/$year"
                    // TODO the following is necessary only if user chooses end date < start dat
                    chosen_end_date.text = chosen_start_date.text
                },
                currYear,
                currMonth,
                currDayOfMonth
            )
            dpd.show()
        }

        // show end date picker dialog
        chosen_end_date.text = chosen_start_date.text
        end_date.setOnClickListener {
            val dpd = DatePickerDialog(
                this.activity!!,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val chosenMonth = month + 1 // months are indexed starting at 0
                    chosen_end_date.text = "$dayOfMonth/$chosenMonth/$year"
                },
                currYear,
                currMonth,
                currDayOfMonth
            )
            dpd.show()
        }

        //confirm new event
        confirm_button.setOnClickListener {
            val eventNameEditText = view.findViewById(R.id.new_event_name) as EditText
            val eventName = eventNameEditText.getText().toString()
            var eventType = Event.EventType.PUBLIC_EVENT
            val evenTypeCheckSwitch =
                view.findViewById(R.id.event_type_switch) as Switch
            if (evenTypeCheckSwitch.isChecked) {
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

            this.dismiss()
            Snackbar.make(view, "Event created successfully!", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }
        //cancel new event
        cancel_button.setOnClickListener {
            this.dismiss()
        }
    }


    override fun onPlaceSelected(place: Place) {
//        Toast.makeText(context,""+p0.name+p0.latLng, Toast.LENGTH_LONG).show()
        this.eventLocation = place
    }

    override fun onError(status: Status) {
        Toast.makeText(this.context, "" + status.toString(), Toast.LENGTH_LONG).show()
    }

}

//fun onButtonPressed(uri: Uri) {
//    listener?.onFragmentInteraction(uri)
//}


//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }

//override fun onDetach() {
//    super.onDetach()
//    listener = null
//}

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
//interface OnFragmentInteractionListener {
//    fun onFragmentInteraction(uri: Uri)
//}

//companion object {
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment NewEvent.
//     */
//    @JvmStatic
//    fun newInstance(param1: String, param2: String) =
//        NewEvent().apply {
//            arguments = Bundle().apply {
//                putString(ARG_PARAM1, param1)
//                putString(ARG_PARAM2, param2)
//            }
//        }
//}
//}

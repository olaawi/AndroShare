package com.example.androshare

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize places + DB
        if (!Places.isInitialized()) {
            Places.initialize(context!!, getString(R.string.places_api_key))
        }
        database = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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


        var startDate = LocalDateTime.now()
        var endDate = LocalDateTime.now()

        chosen_start_date.text =
            startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        chosen_end_date.text =
            endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

        // Show start date picker dialog
        start_date.setOnClickListener {
            val dpd = DatePickerDialog(
                this.activity!!,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    startDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
                    chosen_start_date.text =
                        startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    if (startDate > endDate) {
                        endDate = startDate
                        chosen_end_date.text =
                            endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    }
                },
                startDate.year,
                startDate.monthValue - 1,
                startDate.dayOfMonth
            )
            dpd.show()
        }

        // Show end date picker dialog
        end_date.setOnClickListener {
            val dpd = DatePickerDialog(
                this.activity!!,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    endDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
                    chosen_end_date.text =
                        endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    if (endDate < startDate) {
                        startDate = endDate
                        chosen_start_date.text =
                            startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    }
                },
                endDate.year,
                endDate.monthValue - 1,
                endDate.dayOfMonth
            )
            dpd.show()
        }

        var startTime = LocalDateTime.of(startDate.year, startDate.month, startDate.dayOfMonth, 8, 0)
        var endTime = LocalDateTime.of(endDate.year, endDate.month, endDate.dayOfMonth, 9, 0)

        // In case time has already passed 8:00-9:00
        val now = LocalDateTime.now()
        if(startTime.toLocalTime() < now.toLocalTime()){
            startTime = LocalDateTime.of(startDate.year, startDate.month, startDate.dayOfMonth, now.hour + 1, 0)
            endTime = LocalDateTime.of(endDate.year, endDate.month, endDate.dayOfMonth, now.hour + 1, 0)
        }

        chosen_start_time.text =
            startTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        chosen_end_time.text =
            endTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

        // Show start time picker dialog
        start_time.setOnClickListener {
            val tpd = TimePickerDialog(
                this.activity!!,
                2,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    startTime = LocalDateTime.of(startDate.year, startDate.month, startDate.dayOfMonth, hourOfDay, minute)
                    chosen_start_time.text =
                        startTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                    if (startTime > endTime) {
                        endTime = startTime
                        chosen_end_time.text =
                            endTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                    }
                },
                startTime.hour,
                startTime.minute,
                true
            )
            tpd.show()
        }

        // Show end time picker dialog
        end_time.setOnClickListener {
            val tpd = TimePickerDialog(
                this.activity!!,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    endTime = LocalDateTime.of(endDate.year, endDate.month, endDate.dayOfMonth, hourOfDay, minute)
                    chosen_end_time.text =
                        endTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                    if (endTime < startTime) {
                        startTime = endTime
                        chosen_start_time.text =
                            startTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                    }
                },
                startTime.hour,
                startTime.minute,
                true
            )
            tpd.show()
        }

        // Confirm new event
        confirm_button.setOnClickListener {
            // Title
            val eventTitleEditText = view.findViewById(R.id.new_event_title) as EditText
            var eventTitle = eventTitleEditText.text.toString()
            if (eventTitle == "")
                eventTitle = "My event"

            // Description
            val eventDescriptionEditText = view.findViewById(R.id.new_event_title) as EditText
            val eventDescription = eventDescriptionEditText.text.toString()

            // Start Time


            // End Time


            // Type
            var eventType = Event.EventType.PUBLIC_EVENT
            val eventTypeCheckSwitch =
                view.findViewById(R.id.event_type_switch) as Switch
            if (eventTypeCheckSwitch.isChecked) {
                eventType = Event.EventType.PRIVATE_EVENT
            }

            // Creator
            // TODO get current logged in user
            val eventCreator = User("Hala", "Awisat", "email@gmail.com", "1234567890")

            // And finally create the event ..
            val event = Event(eventTitle, eventDescription, eventCreator, eventType)

            //TODO add event for user (admin)

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

        // Cancel new event
        cancel_button.setOnClickListener {
            this.dismiss()
        }
    }


    // Places methods
    override fun onPlaceSelected(place: Place) {
//        Toast.makeText(context,""+p0.name+p0.latLng, Toast.LENGTH_LONG).show()
        this.eventLocation = place
    }

    override fun onError(status: Status) {
        Toast.makeText(this.context, "" + status.toString(), Toast.LENGTH_LONG).show()
    }

}

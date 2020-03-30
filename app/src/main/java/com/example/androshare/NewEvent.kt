package com.example.androshare

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
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


class NewEvent : Fragment(), PlaceSelectionListener {

    private lateinit var database: FirebaseFirestore
    private lateinit var eventLocation: EventLocation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize places + DB
        Places.initialize(context!!, getString(R.string.places_api_key))
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
        // Autocomplete location: place return fields
        val locationAutocompleteFragment =
            this.childFragmentManager.findFragmentById(R.id.event_location_autocomplete) as AutocompleteSupportFragment
        locationAutocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.LAT_LNG,
                Place.Field.ID,
                Place.Field.NAME
            )
        )
        locationAutocompleteFragment.setOnPlaceSelectedListener(this)

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

        var startTime =
            LocalDateTime.of(startDate.year, startDate.month, startDate.dayOfMonth, 8, 0)
        var endTime = LocalDateTime.of(endDate.year, endDate.month, endDate.dayOfMonth, 9, 0)

        // In case time has already passed 8:00-9:00
        val now = LocalDateTime.now()
        if (startTime.toLocalTime() < now.toLocalTime()) {

            startTime = LocalDateTime.of(
                startDate.year,
                startDate.month,
                startDate.dayOfMonth,
                now.hour,
                0
            )
            endTime =
                LocalDateTime.of(endDate.year, endDate.month, endDate.dayOfMonth, now.hour, 0)
        }

        chosen_start_time.text =
            startTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        chosen_end_time.text =
            endTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

        // Show start time picker dialog
        start_time.setOnClickListener {
            val tpd = TimePickerDialog(
                this.activity!!,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    startTime = LocalDateTime.of(
                        startDate.year,
                        startDate.month,
                        startDate.dayOfMonth,
                        hourOfDay,
                        minute
                    )
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
                    endTime = LocalDateTime.of(
                        endDate.year,
                        endDate.month,
                        endDate.dayOfMonth,
                        hourOfDay,
                        minute
                    )
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

        event_type_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The switch is enabled/checked
                new_event_pin.visibility = View.VISIBLE
            } else {
                // The switch is disabled
                new_event_pin.visibility = View.INVISIBLE
            }
        }

        // Confirm new event
        confirm_button.setOnClickListener {

            // Title
            val eventTitleEditText = view.findViewById(R.id.new_event_title) as EditText
            val eventTitle = eventTitleEditText.text.toString()

            // Description
            val eventDescriptionEditText =
                view.findViewById(R.id.new_event_descriptions) as EditText
            val eventDescription = eventDescriptionEditText.text.toString()

            // Type
            var eventType = Event.EventType.PUBLIC_EVENT
            if (event_type_switch.isChecked) {
                eventType = Event.EventType.PRIVATE_EVENT
            }

            // Password
            var pin = ""
            var confirmPin = ""
            if (eventType == Event.EventType.PRIVATE_EVENT) {
                pin = new_event_pin_text.text.toString()
                confirmPin = new_event_confirm_pin_text.text.toString()
            }

            // Creator is current logged in user
            val account = GoogleSignIn.getLastSignedInAccount(context)
            val eventCreator =
                User(account!!.givenName!!, account.familyName!!, account.email!!, account.id!!)

            // Check if all okay
            val warnings = ArrayList<String>()
            if (eventTitle == "")
                warnings.add("Title cannot be empty\n")
            if (eventDescription == "")
                warnings.add("Description cannot be empty\n")
            if (!::eventLocation.isInitialized)
                warnings.add("Location cannot be empty\n")
            if (eventType == Event.EventType.PRIVATE_EVENT && pin.length < 8)
                warnings.add("Password must be at least 8 characters\n")
            if (eventType == Event.EventType.PRIVATE_EVENT && pin != confirmPin)
                warnings.add("Passwords don't match")

            var warning = ""
            for (w in warnings) {
                warning += w
            }

            if (warnings.size != 0) {

                // Show alert
                val builder = AlertDialog.Builder(context!!,R.style.alertDialogStyle)
                builder.setTitle("Error")
                builder.setMessage(warning)
                builder.setNeutralButton("Ok"){ dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()

            } else {

                // And finally create the event ..
                val event = Event(
                    eventTitle,
                    eventDescription,
                    eventCreator,
                    eventType,
                    startTime,
                    endTime,
                    eventLocation,
                    pin
                )

                // Add event for user
                val userDoc = database.collection("users").document(eventCreator.id)
                userDoc.get()
                    .addOnSuccessListener { document ->
                        val eventsList = document.get("events") as ArrayList<String>
                        eventsList.add(event.id)
                        database.collection("users").document(eventCreator.id)
                            .update("events", eventsList)
                            .addOnSuccessListener {
                                Log.d("NewEvent", "Added event to user")
                            }
                            .addOnFailureListener {
                                Log.e("NewEvent", "Error adding event to user")
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("NewEvent", "Error adding event to user", exception)
                    }

                // Add event to DB
                val doc = database.collection("events").document(event.id)
                doc.get()
                    .addOnSuccessListener { document ->
                        database.collection("events").document(event.id).set(event)
                        Log.d(
                            "NewEvent",
                            "Event added with ID: ${document.id}"
                        )
                        Snackbar.make(
                                activity!!.findViewById(android.R.id.content),
                                "Event created successfully!",
                                Snackbar.LENGTH_SHORT
                            )
                            .setAction("Action", null)
                            .show()
                        fragmentManager!!.popBackStack()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("NewEvent", "Error adding event", exception)
                        Snackbar.make(
                                view,
                                "Failed creating event, please try again",
                                Snackbar.LENGTH_LONG
                            )
                            .setAction("Action", null)
                            .show()
                    }
            }
        }
        // Cancel new event
        cancel_button.setOnClickListener {
            fragmentManager!!.popBackStack()
        }
    }

    // Places methods
    override fun onPlaceSelected(place: Place) {
        Log.d(
            "NewEvent-OnPlaceSelectedListener",
            "lng= " + place.latLng!!.longitude + " lat= " + place.latLng!!.latitude
        )
        eventLocation =
            EventLocation(place.name!!, place.latLng!!.latitude, place.latLng!!.longitude)
    }

    override fun onError(status: Status) {
        Toast.makeText(this.context, "" + status.toString(), Toast.LENGTH_LONG).show()
    }

}

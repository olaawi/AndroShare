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
import android.app.DatePickerDialog
import android.location.Location
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import android.widget.EditText
import android.widget.Switch
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.new_event_dialog.*
import java.time.LocalDateTime
import kotlin.collections.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Dashboard : Fragment(), PlaceSelectionListener, CallbackListener {

    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var eventLocation: Place
    private lateinit var database: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var events: ArrayList<Event?>
    private lateinit var newEventButton: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.param1 = it.getString(ARG_PARAM1)
            this.param2 = it.getString(ARG_PARAM2)
        }

        // initialize places + DB
//        Places.initialize(this.context!!, this.getString(R.string.places_api_key))
        database = FirebaseFirestore.getInstance()

        // create list of the user's event_in_dashboard
        this.events = arrayListOf<Event?>()
        this.events.add(
            Event(
                "Event 1", "this is my first event_in_dashboard",
                User("Ola", "Awisat", "ola@gmail", "0"),
                Event.EventType.PUBLIC_EVENT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                EventLocation("dummy 1", 0.0, 0.0),
                "0000"
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
                "0000"
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
                "0000"
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
                "0000"
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
                "0000"
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
                "0000"
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
                "0000"
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
                "0000"
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


    }

    private fun onEventClicked(event: Event) {
        val eventPageFragment = EventPage(event)
        val transaction = fragmentManager!!.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction
            .add(android.R.id.content, eventPageFragment)
            .addToBackStack(null)
            .commit()
//        Toast.makeText(context, "Clicked: ${event.title}", Toast.LENGTH_LONG).show()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onDataReceived(data: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
    }

    override fun onPlaceSelected(place: Place) {
//        Toast.makeText(context,""+p0.name+p0.latLng, Toast.LENGTH_LONG).show()
        this.eventLocation = place
    }

    override fun onError(status: Status) {
        Toast.makeText(this.context, "" + status.toString(), Toast.LENGTH_LONG).show()
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

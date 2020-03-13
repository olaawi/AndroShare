package com.example.androshare

import EventAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

// TODO: change max radius
// maximum radius to search events near me - in meters
private const val MAX_RADIUS = 5000

class NearMe : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: Dashboard.OnFragmentInteractionListener? = null

    //Provides the entry point to the Fused Location Provider API.
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    //Represents a geographical location.
    private var currentLocation: Location? = null
    private lateinit var database: FirebaseFirestore
    private lateinit var events: ArrayList<Event?>
    private lateinit var currentEvent: Event
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter

//    private var mLatitudeLabel: String? = null
//    private var mLongitudeLabel: String? = null
//    private var mLatitudeText: TextView? = null
//    private var mLongitudeText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mLatitudeLabel = "Latitude"
//        mLongitudeLabel = "Longitude"
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
        database = FirebaseFirestore.getInstance()
        this.events = arrayListOf<Event?>()

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_near_me, container, false)
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerView = view.findViewById(R.id.recyclerViewNearMe)
        this.recyclerView.layoutManager = LinearLayoutManager(this.context)
//        this.eventAdapter =
//            EventAdapter(this.context!!, this.events) { event: Event -> onEventClicked(event) }
//        this.recyclerView.adapter = this.eventAdapter
//        mLatitudeText = view.findViewById<View>(R.id.latitude_text) as TextView
//        mLongitudeText = view.findViewById<View>(R.id.longitude_text) as TextView
    }

    private fun onEventClicked(event: Event) {
        // TODO implement
//        val eventPageFragment = EventPage(event)
//        val transaction = fragmentManager!!.beginTransaction()
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//        transaction
//            .add(android.R.id.content, eventPageFragment)
//            .addToBackStack(null)
//            .commit()
//        Toast.makeText(context, "Clicked: ${event.title}", Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    private fun findEventsNearMe() {
        events.clear()
        database.collection("events")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("findEventsNearMe 1", "${document.id} => ${document.data}")
                    val eventLng: Double = document.get("location.longitude")!! as Double
                    val eventLat: Double = document.get("location.latitude")!! as Double
                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        eventLat,
                        eventLng,
                        this.currentLocation!!.latitude,
                        this.currentLocation!!.longitude,
                        distance
                    )
                    if (distance[0] <= MAX_RADIUS) {
                        Log.d("findEventsNearMe 2", "Checked Distance")
                        var eventType: Event.EventType = Event.EventType.PUBLIC_EVENT
                        if (document.get("type")!! == "PRIVATE_EVENT") {
                            eventType = Event.EventType.PRIVATE_EVENT
                        }
                        val startTime = LocalDateTime.of(
                            (document.get("startTime.year")!! as Long).toInt(),
                            (document.get("startTime.monthValue")!! as Long).toInt(),
                            (document.get("startTime.dayOfMonth")!! as Long).toInt(),
                            (document.get("startTime.hour")!! as Long).toInt(),
                            (document.get("startTime.minute")!! as Long).toInt()
                        )
                        val endTime = LocalDateTime.of(
                            (document.get("endTime.year")!! as Long).toInt(),
                            (document.get("endTime.monthValue")!! as Long).toInt(),
                            (document.get("endTime.dayOfMonth")!! as Long).toInt(),
                            (document.get("endTime.hour")!! as Long).toInt(),
                            (document.get("endTime.minute")!! as Long).toInt()
                        )
                        currentEvent = Event(
                            (document.get("title") as String?)!!,
                            (document.get("description") as String?)!!,
                            User(
                                document.get("creator.givenName") as String,
                                document.get("creator.familyName") as String,
                                document.get("creator.email") as String,
                                document.get("creator.id") as String
                            ),
                            eventType,
                            startTime,
                            endTime,
                            EventLocation(
                                document.get("location.name")!! as String,
                                document.get("location.latitude")!! as Double,
                                document.get("location.longitude")!! as Double
                            ),
                            (document.get("id")!! as Long).toInt()
                        )
                        events.add(currentEvent)
                        Log.d(
                            "findEventsNearMe 3",
                            events[0]!!.title + " " + events[0]!!.creator.email
                        )
                    }
                }
                // TODO: add fun to view items
                this.eventAdapter =
                    EventAdapter(
                        this.context!!,
                        this.events
                    ) { event: Event -> onEventClicked(event) }
                this.recyclerView.adapter = this.eventAdapter
            }
            .addOnFailureListener { exception ->
                Log.d("findEventsNearMe", "Error getting documents: ", exception)
            }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation
            .addOnCompleteListener(this.activity!!) { task ->
                if (task.isSuccessful && task.result != null) {
                    currentLocation = task.result
                    findEventsNearMe()
//                    mLatitudeText!!.text = "Lognitude"+":   "+
//                            (currentLocation )!!.latitude
//                    mLongitudeText!!.text = "Latitude"+":   "+
//                            (currentLocation )!!.longitude
                    Log.d("getLastLocation - Longitude", (currentLocation)!!.longitude.toString())
                    Log.d("getLastLocation - Latitude", (currentLocation)!!.latitude.toString())
                } else {
                    Log.d("getLastLocation", "getLastLocation:exception", task.exception)
                    Toast.makeText(context, "No location detected", Toast.LENGTH_LONG).show()

                }
            }
    }


    /**
     * Shows a [].
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * *
     * @param actionStringId   The text of the action item.
     * *
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(
        mainTextStringId: String, actionStringId: Int,
        listener: View.OnClickListener
    ) {

        Toast.makeText(context, mainTextStringId, Toast.LENGTH_LONG).show()
    }

    ////////////////////
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this.activity!!,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this.activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
                "requestPermissions",
                "Displaying permission rationale to provide additional context."
            )
            showSnackbar("permission_rationale", android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    startLocationPermissionRequest()
                })

        } else {
            Log.i("requestPermissions", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i("onRequestPermissionsResult", "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("onRequestPermissionsResult", "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation()
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar("permission_denied_explanation", R.string.settings,
                    View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })
            }
        }
    }
    ////////////////////

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
        listener = null
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
        //        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NearMe.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NearMe().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

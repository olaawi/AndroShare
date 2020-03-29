package com.example.androshare

import EventAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime


// TODO: change max radius
// maximum radius to search events near me - in meters
private const val MAX_RADIUS = 5000

class NearMe : Fragment() {

    //Provides the entry point to the Fused Location Provider API.
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    //Represents a geographical location.
    private var currentLocation: Location? = null
    private lateinit var database: FirebaseFirestore
    private lateinit var events: ArrayList<Event?>
    private lateinit var currentEvent: Event
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
        database = FirebaseFirestore.getInstance()
        this.events = arrayListOf<Event?>()
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
        val eventPageFragment = JoinEvent(event)
        Log.d("NearMe - onEventClicked", event.id)
        val transaction = fragmentManager!!.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction
            .add(android.R.id.content, eventPageFragment)
            .addToBackStack(null)
            .commit()
//        Toast.makeText(context, "Clicked: ${event.title}", Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
            if (!isLocationEnabled()) {
                Toast.makeText(context, "Please enable location", Toast.LENGTH_LONG).show()
            }
        } else if (!isLocationEnabled()) {
            Toast.makeText(context, "Please enable location", Toast.LENGTH_LONG).show()
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
                    Log.d("NearMe-findEventsNearMe1", "${document.id} => ${document.data}")
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
                        var eventType: Event.EventType = Event.EventType.PUBLIC_EVENT
                        var pin = "0000"
                        if (document.get("type")!! == "PRIVATE_EVENT") {
                            eventType = Event.EventType.PRIVATE_EVENT
                            pin = document.get("pin")!! as String
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
                            pin
                        )
                        currentEvent.id = document.get("id")!! as String
                        events.add(currentEvent)
                    }
                }
                // TODO exclude events i'm already in
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
                if (task.isSuccessful) {
                    if (task.result == null) {
                        requestNewLocationData()
                    } else {
                        currentLocation = task.result
                        findEventsNearMe()
                        Log.d(
                            "getLastLocation - Longitude",
                            (currentLocation)!!.longitude.toString()
                        )
                        Log.d("getLastLocation - Latitude", (currentLocation)!!.latitude.toString())
                    }
                } else {
                    Toast.makeText(context, "No location detected", Toast.LENGTH_LONG).show()

                }
            }

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
            Log.d(
                "requestPermissions",
                "shouldProvideRationale = true"
            )
            Snackbar.make(
                this.view!!,
                "Please allow access to location to activate this service",
                Snackbar.LENGTH_LONG
            ).setAction(android.R.string.ok) {
                // Request permission
                startLocationPermissionRequest()
            }
        } else {
            Log.d("requestPermissions", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            currentLocation = mLastLocation
//            findViewByIdewById<TextView>(R.id.latTextView).text = mLastLocation.latitude.toString()
//            findViewById<TextView>(R.id.lonTextView).text = mLastLocation.longitude.toString()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        Log.i("onRequestPermissionsResult", "onRequestPermissionResult")
//        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
//            if (grantResults.isEmpty()) {
//                // If user interaction was interrupted, the permission request is cancelled and you
//                // receive empty arrays.
//                Log.i("onRequestPermissionsResult", "User interaction was cancelled.")
//            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted.
//                getLastLocation()
//            } else {
//                // Permission denied.
//
//                // Notify the user via a SnackBar that they have rejected a core permission for the
//                // app, which makes the Activity useless. In a real app, core permissions would
//                // typically be best requested during a welcome-screen flow.
//
//                // Additionally, it is important to remember that a permission might have been
//                // rejected without asking the user for permission (device policy or "Never ask
//                // again" prompts). Therefore, a user interface affordance is typically implemented
//                // when permissions are denied. Otherwise, your app could appear unresponsive to
//                // touches or interactions which have required permissions.
//
//                Snackbar.make(
//                    this.view!!,
//                    "permission_denied_explanation",
//                    Snackbar.LENGTH_LONG
//                ).setAction(R.string.settings) {
//                    // Build intent that displays the App settings screen.
//                    val intent = Intent()
//                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                    val uri = Uri.fromParts(
//                        "package",
//                        BuildConfig.APPLICATION_ID, null
//                    )
//                    intent.data = uri
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(intent)
//                }
//            }
//        }
//    }
    ////////////////////

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}

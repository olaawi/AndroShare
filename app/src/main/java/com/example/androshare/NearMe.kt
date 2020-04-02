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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
        this.events = arrayListOf()
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

        // set up refresh layout
        val refreshView = view.findViewById<SwipeRefreshLayout>(R.id.near_me_refresh)
        refreshView.setColorSchemeResources(R.color.accentColor)
        refreshView.setProgressBackgroundColorSchemeResource(R.color.primaryDarkColor)
        refreshView.setOnRefreshListener {
            // Reload data from database
            getLastLocation()
            refreshView.isRefreshing = false
        }

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

    @Suppress("UNCHECKED_CAST")
    private fun findEventsNearMe() {
        events.clear()
        database.collection("events")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("NearMe-findEventsNearMe1", "${document.id} => ${document.data}")
                    val participantsList = document.get("participants")!! as ArrayList<String>
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    val eventStartDate = LocalDate.of(
                        (document.get("startTime.year")!! as Long).toInt(),
                        (document.get("startTime.monthValue")!! as Long).toInt(),
                        (document.get("startTime.dayOfMonth")!! as Long).toInt()
                    )
                    val eventEndDate = LocalDate.of(
                        (document.get("endTime.year")!! as Long).toInt(),
                        (document.get("endTime.monthValue")!! as Long).toInt(),
                        (document.get("endTime.dayOfMonth")!! as Long).toInt()
                    )
                    val today = LocalDate.now()
                    // if i'm already a participant or the event is not today
                    if (participantsList.contains(account!!.id!!) || !(today.isEqual(eventStartDate) ||
                                (today.isAfter(eventStartDate) && today.isBefore(eventEndDate)) || today.isEqual(
                            eventEndDate
                        ))
                    ) {
                        Log.e("find", "event not today" + document.get("title").toString())
                        Log.e(
                            "find",
                            today.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                        )
                        continue
                    }
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
                if (events.isEmpty()) {
                    // Show alert
                    val builder = AlertDialog.Builder(context!!, R.style.alertDialogStyle)
                    builder.setTitle("Info")
                    builder.setMessage("No events are currently taking place here.")
                    builder.setNeutralButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                } else {
                    this.eventAdapter =
                        EventAdapter(
                            this.context!!,
                            this.events
                        ) { event: Event -> onEventClicked(event) }
                    this.recyclerView.adapter = this.eventAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.d("findEventsNearMe", "Error getting documents: ", exception)
            }
    }

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
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}

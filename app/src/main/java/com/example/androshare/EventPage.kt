package com.example.androshare

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.event_page_menu_dialog.view.*
import kotlinx.android.synthetic.main.fragment_event_page.*
import kotlinx.android.synthetic.main.fragment_event_page.view.*
import java.io.OutputStream
import java.time.LocalDateTime

private const val EVENT_RADIUS = 1000

class EventPage(private val event: Event) : Fragment(), IOnBackPressed {

    private lateinit var storage: FirebaseStorage
    private lateinit var images: ArrayList<Image>
    lateinit var imageAdapter: ImageAdapter
    private lateinit var database: FirebaseFirestore
    private var isAdmin: Boolean = false
    private lateinit var account: GoogleSignInAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = FirebaseStorage.getInstance()
        images = ArrayList()
        database = FirebaseFirestore.getInstance()
        account = GoogleSignIn.getLastSignedInAccount(context)!!
        isAdmin = event.isAdmin(account.id!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_page, container, false)
    }

    @SuppressLint("InflateParams")
    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.event_back).setOnClickListener {
            activity!!.onBackPressed()
        }
        view.findViewById<ImageView>(R.id.event_add).setOnClickListener {
            uploadImage()
        }

        // set up refresh layout
        val refreshView = view.findViewById<SwipeRefreshLayout>(R.id.event_page_refresh)
        refreshView.setColorSchemeResources(R.color.accentColor)
        refreshView.setProgressBackgroundColorSchemeResource(R.color.primaryDarkColor)
        refreshView.setOnRefreshListener {
            // Reload data from database if something changed
            initGrid(view)
            refreshView.isRefreshing = false
        }

        view.findViewById<ImageView>(R.id.event_more).setOnClickListener {
            val popUpMenu = PopupMenu(context, it)
            popUpMenu.menu.clear()
            if (isAdmin) {
                popUpMenu.inflate(R.menu.event_page_admin_menu)
            } else {
                popUpMenu.inflate(R.menu.event_page_participant_menu)
            }
            popUpMenu.gravity = Gravity.RELATIVE_LAYOUT_DIRECTION
            popUpMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.event_page_leave_event -> {
                        // remove user from participants
                        database.collection("events").document(event.id)
                            .get()
                            .addOnSuccessListener { eventDocument ->
                                val usersList =
                                    eventDocument.get("participants") as ArrayList<String>
                                usersList.remove(account.id!!)
                                event.participants.remove(account.id!!)
                                database.collection("events").document(event.id)
                                    .update("participants", usersList)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "EventPage",
                                            "left event with ID: ${eventDocument.id}"
                                        )
                                        // remove event for user
                                        database.collection("users").document(account.id!!)
                                            .get()
                                            .addOnSuccessListener { document ->
                                                val eventsList =
                                                    document.get("events") as ArrayList<String>
                                                eventsList.remove(event.id)
                                                database.collection("users").document(account.id!!)
                                                    .update("events", eventsList)
                                                    .addOnSuccessListener {
                                                        Log.d("EventPage", "Removed event for user")
                                                        Snackbar.make(
                                                                view,
                                                                "Successfully left event!",
                                                                Snackbar.LENGTH_LONG
                                                            )
                                                            .setAction("Action", null)
                                                            .show()
                                                    }
                                                    .addOnFailureListener {
                                                        Log.e(
                                                            "EventPage",
                                                            "Error removing event for user"
                                                        )
                                                    }
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e(
                                                    "EventPage",
                                                    "Error removing event for user",
                                                    exception
                                                )
                                            }
                                    }
                                    .addOnFailureListener {
                                        Log.e("EventPage", "Error leaving event")
                                    }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("EventPage", "Error leaving event", exception)
                            }

                        fragmentManager!!.popBackStack()
                    }

                    R.id.event_page_add_participant -> {
                        //Inflate the dialog with custom view
                        val mDialogView = LayoutInflater.from(context)
                            .inflate(R.layout.event_page_menu_dialog, null)
                        //AlertDialogBuilder
                        val mBuilder = AlertDialog.Builder(context)
                            .setView(mDialogView)
                            .show()
                        mDialogView.event_page_dialog_confirm_button.setOnClickListener {
                            val emailEditText =
                                mDialogView.findViewById(R.id.event_page_dialog_email) as EditText
                            val email = emailEditText.text.toString()
                            database.collection("users").whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { result ->
                                    if (result.isEmpty) {
                                        Log.d("EventPage", "No such user in database")
                                        Snackbar.make(
                                                view,
                                                "Failed adding participant, no such user in database!",
                                                Snackbar.LENGTH_LONG
                                            )
                                            .setAction("Action", null)
                                            .show()
                                    }
                                    for (document in result) {
                                        val emailId = document.get("id") as String
                                        // if user is already a participant
                                        if (event.isParticipant(emailId)) {
                                            Snackbar.make(
                                                    view,
                                                    "User is already a participant",
                                                    Snackbar.LENGTH_LONG
                                                )
                                                .setAction("Action", null)
                                                .show()
                                        } else {
                                            val eventsList =
                                                document.get("events") as ArrayList<String>
                                            eventsList.add(event.id)
                                            database.collection("users").document(emailId)
                                                .update("events", eventsList)
                                                .addOnSuccessListener {
                                                    Log.d("EventPage", "Added participant")
                                                    // add user to event
                                                    database.collection("events").document(event.id)
                                                        .get()
                                                        .addOnSuccessListener { document ->
                                                            val usersList =
                                                                document.get("participants") as ArrayList<String>
                                                            usersList.add(emailId)
                                                            database.collection("events")
                                                                .document(event.id)
                                                                .update("participants", usersList)
                                                                .addOnSuccessListener {
                                                                    event.addParticipant(emailId)
                                                                    Log.d(
                                                                        "EventPage",
                                                                        "Joined event with ID: ${document.id}"
                                                                    )
                                                                    Snackbar.make(
                                                                            view,
                                                                            "Successfully added participant!",
                                                                            Snackbar.LENGTH_LONG
                                                                        )
                                                                        .setAction("Action", null)
                                                                        .show()
                                                                }
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    Log.e(
                                                        "EventPage",
                                                        "Error adding event for user"
                                                    )
                                                }
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "EventPage",
                                        "Error adding new participant",
                                        exception
                                    )
                                }
                            mBuilder.dismiss()
                        }
                        mDialogView.event_page_dialog_cancel_button.setOnClickListener {
                            //dismiss dialog
                            mBuilder.dismiss()
                        }
                    }

                    R.id.event_page_remove_participant -> {
                        //Inflate the dialog with custom view
                        val mDialogView = LayoutInflater.from(context)
                            .inflate(R.layout.event_page_menu_dialog, null)
                        //AlertDialogBuilder
                        val mBuilder = AlertDialog.Builder(context)
                            .setView(mDialogView)
                            .show()
                        mDialogView.event_page_dialog_confirm_button.setOnClickListener {
                            val emailEditText =
                                mDialogView.findViewById(R.id.event_page_dialog_email) as EditText
                            val email = emailEditText.text.toString()
                            database.collection("users").whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { result ->
                                    if (result.isEmpty) {
                                        Log.d("EvantPage", "User not in DB")
                                        Snackbar.make(
                                                view,
                                                "Failed removing participant, user is not in database!",
                                                Snackbar.LENGTH_LONG
                                            )
                                            .setAction("Action", null)
                                            .show()
                                    }
                                    for (document in result) {
                                        val emailId = document.get("id") as String
                                        // if user is not a participant
                                        if (!event.isParticipant(emailId)) {
                                            Snackbar.make(
                                                    view,
                                                    "User is not a participant",
                                                    Snackbar.LENGTH_LONG
                                                )
                                                .setAction("Action", null)
                                                .show()
                                        } else if (event.isAdmin(emailId)) {
                                            Snackbar.make(
                                                    view,
                                                    "Cannot remove an admin",
                                                    Snackbar.LENGTH_LONG
                                                )
                                                .setAction("Action", null)
                                                .show()
                                        } else {
                                            val eventsList =
                                                document.get("events") as ArrayList<String>
                                            eventsList.remove(event.id)
                                            database.collection("users").document(emailId)
                                                .update("events", eventsList)
                                                .addOnSuccessListener {
                                                    Log.d("EventPage", "Removed participant")
                                                    // remove user from event
                                                    database.collection("events").document(event.id)
                                                        .get()
                                                        .addOnSuccessListener { document ->
                                                            val usersList =
                                                                document.get("participants") as ArrayList<String>
                                                            usersList.remove(emailId)
                                                            database.collection("events")
                                                                .document(event.id)
                                                                .update("participants", usersList)
                                                                .addOnSuccessListener {
                                                                    event.participants.remove(
                                                                        emailId
                                                                    )
                                                                    Log.d(
                                                                        "EventPage",
                                                                        "Removed participant"
                                                                    )
                                                                    Snackbar.make(
                                                                            view,
                                                                            "Successfully removed participant!",
                                                                            Snackbar.LENGTH_LONG
                                                                        )
                                                                        .setAction("Action", null)
                                                                        .show()
                                                                }
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    Log.e(
                                                        "EventPage",
                                                        "Error removing participant from event"
                                                    )
                                                }
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "EventPage",
                                        "Error removing participant, user is not in database",
                                        exception
                                    )
                                }
                            mBuilder.dismiss()
                        }
                        mDialogView.event_page_dialog_cancel_button.setOnClickListener {
                            //dismiss dialog
                            mBuilder.dismiss()
                        }
                    }

                    R.id.event_page_add_admin -> {
                        //Inflate the dialog with custom view
                        val mDialogView = LayoutInflater.from(context)
                            .inflate(R.layout.event_page_menu_dialog, null)
                        //AlertDialogBuilder
                        val mBuilder = AlertDialog.Builder(context)
                            .setView(mDialogView)
                            .show()
                        mDialogView.event_page_dialog_confirm_button.setOnClickListener {
                            val emailEditText =
                                mDialogView.findViewById<EditText>(R.id.event_page_dialog_email) as EditText
                            val email = emailEditText.text.toString()
                            database.collection("users").whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { result ->
                                    if (result.isEmpty) {
                                        Log.d("EventPage", "User is not in DB")
                                        Snackbar.make(
                                                view,
                                                "Failed adding admin, user is not in database!",
                                                Snackbar.LENGTH_LONG
                                            )
                                            .setAction("Action", null)
                                            .show()
                                    }
                                    for (document in result) {
                                        val emailId = document.get("id") as String
                                        // if user is not a participant
                                        if (event.isAdmin(emailId)) {
                                            Snackbar.make(
                                                    view,
                                                    "User is already an admin",
                                                    Snackbar.LENGTH_LONG
                                                )
                                                .setAction("Action", null)
                                                .show()
                                        } else if (!event.isParticipant(emailId)) {
                                            Snackbar.make(
                                                    view,
                                                    "User is not a participant in this event",
                                                    Snackbar.LENGTH_LONG
                                                )
                                                .setAction("Action", null)
                                                .show()
                                        } else {
                                            // add admin to event
                                            database.collection("events").document(event.id)
                                                .get()
                                                .addOnSuccessListener { documentEvent ->
                                                    val usersList =
                                                        documentEvent.get("admins") as ArrayList<String>
                                                    usersList.add(emailId)
                                                    database.collection("events")
                                                        .document(event.id)
                                                        .update("admins", usersList)
                                                        .addOnSuccessListener {
                                                            event.addAdmin(emailId)
                                                            Log.d(
                                                                "EventPage",
                                                                "Added admin"
                                                            )
                                                            Snackbar.make(
                                                                    view,
                                                                    "Successfully added admin!",
                                                                    Snackbar.LENGTH_LONG
                                                                )
                                                                .setAction("Action", null)
                                                                .show()
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    Log.e(
                                                        "EventPage",
                                                        "Error adding admin"
                                                    )
                                                }
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "EventPage",
                                        "Error adding admin, user is not in database",
                                        exception
                                    )
                                }
                            mBuilder.dismiss()
                        }
                        mDialogView.event_page_dialog_cancel_button.setOnClickListener {
                            //dismiss dialog
                            mBuilder.dismiss()
                        }
                    }

                    R.id.event_page_remove_admin -> {
                        //Inflate the dialog with custom view
                        val mDialogView = LayoutInflater.from(context)
                            .inflate(R.layout.event_page_menu_dialog, null)
                        //AlertDialogBuilder
                        val mBuilder = AlertDialog.Builder(context)
                            .setView(mDialogView)
                            .show()
                        mDialogView.event_page_dialog_confirm_button.setOnClickListener {
                            val emailEditText =
                                mDialogView.findViewById<EditText>(R.id.event_page_dialog_email) as EditText
                            val email = emailEditText.text.toString()
                            database.collection("users").whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener { result ->
                                    if (result.isEmpty) {
                                        Log.d("EventPage", "User is not in DB")
                                        Snackbar.make(
                                                view,
                                                "Failed removing admin, email is not in database!",
                                                Snackbar.LENGTH_LONG
                                            )
                                            .setAction("Action", null)
                                            .show()
                                    }
                                    for (document in result) {
                                        val emailId = document.get("id") as String
                                        // if user is not an admin
                                        if (!event.isAdmin(emailId)) {
                                            Snackbar.make(
                                                    view,
                                                    "User is not an admin",
                                                    Snackbar.LENGTH_LONG
                                                )
                                                .setAction("Action", null)
                                                .show()
                                        } else {
                                            // remove admin from event
                                            database.collection("events").document(event.id)
                                                .get()
                                                .addOnSuccessListener { documentEvent ->
                                                    val usersList =
                                                        documentEvent.get("admins") as ArrayList<*>
                                                    usersList.remove(emailId)
                                                    database.collection("events")
                                                        .document(event.id)
                                                        .update("admins", usersList)
                                                        .addOnSuccessListener {
                                                            event.admins.remove(emailId)
                                                            Log.d(
                                                                "EventPage",
                                                                "Removed admin"
                                                            )
                                                            Snackbar.make(
                                                                    view,
                                                                    "Successfully removed admin!",
                                                                    Snackbar.LENGTH_LONG
                                                                )
                                                                .setAction("Action", null)
                                                                .show()
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    Log.e(
                                                        "EventPage",
                                                        "Error removing admin"
                                                    )
                                                }
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "EventPage",
                                        "Error removing admin, user is not in database",
                                        exception
                                    )
                                }
                            mBuilder.dismiss()
                        }
                        mDialogView.event_page_dialog_cancel_button.setOnClickListener {
                            //dismiss dialog
                            mBuilder.dismiss()
                        }
                    }

                    R.id.event_page_delete_event -> {
                        var removedEventFromUsersCount = 0
                        // remove event for all participants
                        database.collection("events").document(event.id)
                            .get()
                            .addOnSuccessListener { eventDocument ->
                                val usersList =
                                    eventDocument.get("participants") as ArrayList<String>
                                for (userId in usersList) {
                                    database.collection("users").document(userId)
                                        .get()
                                        .addOnSuccessListener { currentUserDocument ->
                                            val eventsList =
                                                currentUserDocument.get("events") as ArrayList<String>
                                            eventsList.remove(event.id)
                                            database.collection("users").document(userId)
                                                .update("events", eventsList)
                                                .addOnSuccessListener {
                                                    Log.d(
                                                        "EventPage",
                                                        "removed event for participant"
                                                    )
                                                    removedEventFromUsersCount++
                                                }
                                        }
                                        .addOnFailureListener {
                                            Log.e("EventPage", "Participant is not a user")
                                        }
                                }
                                // done removing event for all users
                                if (removedEventFromUsersCount == usersList.size) {
                                    database.collection("events").document(event.id)
                                        .delete()
                                        .addOnSuccessListener {
                                            Snackbar.make(
                                                    view,
                                                    "Successfully deleted event!",
                                                    Snackbar.LENGTH_LONG
                                                )
                                                .setAction("Action", null)
                                                .show()
                                        }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("EventPage", "Error deleting event", exception)
                            }
                    }
                }
                true
            }
            popUpMenu.show()
        }

        view.findViewById<ImageView>(R.id.event_download).setOnClickListener {
            for (image in images) {
                if (image.isSelected) {
                    downloadImage(image.uri, image.id)
                    image.toggleSelect()
                }
            }

            imageAdapter.mode = ImageAdapter.MODE.REGULAR
            imageAdapter.notifyDataSetChanged()
            setRegularLayout()

        }

        view.findViewById<ImageView>(R.id.event_delete).setOnClickListener {
            for (image in images) {
                if (image.isSelected) {
                    deleteImage(image.uri)
                    image.toggleSelect()
                }
            }
            imageAdapter.mode = ImageAdapter.MODE.REGULAR
            imageAdapter.notifyDataSetChanged()
            setRegularLayout()
        }

        view.findViewById<LinearLayout>(R.id.event_bar).setOnClickListener {
            val eventPageFragment = ParticipantsList(event)
            val transaction = fragmentManager!!.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction
                .add(android.R.id.content, eventPageFragment)
                .addToBackStack(null)
                .commit()
        }
        view.event_title.text = event.title
        view.event_description.text = event.description
        view.event_page_time.text = event.getTime()
        view.event_page_location.text = event.location.name
        initParticipantsList()
        initGrid(view)
        ViewCompat.setNestedScrollingEnabled(view.findViewById(R.id.event_bar), true)
    }

    private fun downloadImage(uri: Uri, id: String) {
        Glide.with(this).asBitmap().load(uri)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    insertImage(context!!.contentResolver, resource, id)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    return
                }
            })
    }

    @Suppress("SameParameterValue", "SameParameterValue")
    private fun insertImage(cr: ContentResolver, source: Bitmap, title: String?): String? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        var url: Uri? = null
        var stringUrl: String? = null
        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val imageOut: OutputStream? = cr.openOutputStream(url!!)
            try {
                source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut)
            } finally {
                imageOut!!.close()
            }
        } catch (e: Exception) {
            if (url != null) {
                cr.delete(url, null, null)
                url = null
            }
        }
        if (url != null) {
            stringUrl = url.toString()
        }
        return stringUrl
    }


    private fun uploadImage() {
        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request the permission.
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            // Permission has already been granted
            pickImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    pickImage()
                } else {
                    // permission denied, boo!
                    Toast.makeText(
                        context,
                        "Permission must be granted in order to upload photos!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        // When an Image is picked
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK
            && null != data
        ) {
            val pickedImages = ArrayList<Image>()
            var imageUri: Uri?

            if (data.clipData != null) {
                // Two or more images we picked
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    imageUri = data.clipData!!.getItemAt(i).uri
                    pickedImages.add(Image(imageUri))
                }
            } else if (data.data != null) {
                // One image was picked
                imageUri = data.data
                pickedImages.add(Image(imageUri!!))
            }

            for (image in pickedImages) {
                // Upload to storage

                val exifInterface =
                    ExifInterface(context!!.contentResolver.openInputStream(image.uri)!!)

                var exif = ""
                exif += "\nIMAGE_LENGTH: " + exifInterface.getAttribute(TAG_IMAGE_LENGTH)
                exif += "\nIMAGE_WIDTH: " + exifInterface.getAttribute(TAG_IMAGE_WIDTH)
                exif += "\n DATETIME: " + exifInterface.getAttribute(TAG_DATETIME)
                exif += "\n TAG_MAKE: " + exifInterface.getAttribute(TAG_MAKE)
                exif += "\n TAG_MODEL: " + exifInterface.getAttribute(TAG_MODEL)
                exif += "\n TAG_ORIENTATION: " + exifInterface.getAttribute(TAG_ORIENTATION)
                exif += "\n TAG_WHITE_BALANCE: " + exifInterface.getAttribute(TAG_WHITE_BALANCE)
                exif += "\n TAG_FOCAL_LENGTH: " + exifInterface.getAttribute(TAG_FOCAL_LENGTH)
                exif += "\n TAG_FLASH: " + exifInterface.getAttribute(TAG_FLASH)
                exif += "\nGPS related:"
                exif += "\n TAG_GPS_DATESTAMP: " + exifInterface.getAttribute(TAG_GPS_DATESTAMP)
                exif += "\n TAG_GPS_TIMESTAMP: " + exifInterface.getAttribute(TAG_GPS_TIMESTAMP)
                exif += "\n TAG_GPS_LATITUDE: " + exifInterface.getAttribute(TAG_GPS_LATITUDE)
                exif += "\n TAG_GPS_LATITUDE_REF: " + exifInterface.getAttribute(
                    TAG_GPS_LATITUDE_REF
                )
                exif += "\n TAG_GPS_LONGITUDE: " + exifInterface.getAttribute(TAG_GPS_LONGITUDE)
                exif += "\n TAG_GPS_LONGITUDE_REF: " + exifInterface.getAttribute(
                    TAG_GPS_LONGITUDE_REF
                )
                exif += "\n TAG_GPS_PROCESSING_METHOD: " + exifInterface.getAttribute(
                    TAG_GPS_PROCESSING_METHOD
                )
                Log.d("exif", exif)

                if (!isAdmin && (exifInterface.getAttribute(TAG_GPS_LONGITUDE) == null ||
                            exifInterface.getAttribute(TAG_GPS_LATITUDE) == null ||
                            exifInterface.getAttribute(TAG_DATETIME) == null ||
                            exifInterface.getAttribute(TAG_GPS_LONGITUDE_REF) == null ||
                            exifInterface.getAttribute(TAG_GPS_LATITUDE_REF) == null
                            )
                ) {
                    Log.e("EventPage", "Image doesn't have the relevant tags")
                    return
                }
                val distance = FloatArray(1)
                val imageLongitudeAttr: String
                val imageLatitudeAttr: String
                val imageLongitudeRef: String
                val imageLatitudeRef: String
                var imageDateTime = LocalDateTime.now()
                var eventStart = LocalDateTime.now()
                var eventEnd = LocalDateTime.now()
                if (!isAdmin) {
                    // format longitude&latitude
                    imageLongitudeAttr =
                        exifInterface.getAttribute(TAG_GPS_LONGITUDE) as String
                    imageLatitudeAttr =
                        exifInterface.getAttribute(TAG_GPS_LATITUDE) as String
                    imageLongitudeRef =
                        exifInterface.getAttribute(TAG_GPS_LONGITUDE_REF) as String
                    imageLatitudeRef =
                        exifInterface.getAttribute(TAG_GPS_LATITUDE_REF) as String
                    var imageLatitude: Double
                    var imageLongitude: Double
                    if (imageLatitudeRef == "N") {
                        imageLatitude = this.convertToDegree(imageLatitudeAttr)!!
                    } else {
                        imageLatitude = 0 - this.convertToDegree(imageLatitudeAttr)!!
                    }

                    if (imageLongitudeRef == "E") {
                        imageLongitude = this.convertToDegree(imageLongitudeAttr)!!
                    } else {
                        imageLongitude = 0 - this.convertToDegree(imageLongitudeAttr)!!
                    }

                    val imageDateTimeArr =
                        (exifInterface.getAttribute(TAG_DATETIME) as String).split(" ")
                            .toTypedArray()
                    val imageDateArr = imageDateTimeArr[0].split(":").toTypedArray()
                    val imageTimeArr = imageDateTimeArr[1].split(":").toTypedArray()
                    imageDateTime = LocalDateTime.of(
                        imageDateArr[0].toInt(),
                        imageDateArr[1].toInt(),
                        imageDateArr[2].toInt(),
                        imageTimeArr[0].toInt(),
                        imageTimeArr[1].toInt(),
                        imageTimeArr[2].toInt()
                    )
                    eventStart = LocalDateTime.of(
                        event.startTime.year,
                        event.startTime.month,
                        event.startTime.dayOfMonth,
                        event.startTime.hour,
                        event.startTime.minute,
                        event.startTime.second
                    )
                    eventEnd =
                        LocalDateTime.of(
                            event.endTime.year,
                            event.endTime.month,
                            event.endTime.dayOfMonth,
                            event.endTime.hour,
                            event.endTime.minute,
                            event.endTime.second
                        )


                    Location.distanceBetween(
                        event.location.latitude,
                        event.location.longitude,
                        imageLatitude,
                        imageLongitude,
                        distance
                    )
                }
                // check location, date and time
                if (isAdmin || (distance[0] <= EVENT_RADIUS && imageDateTime.isAfter(eventStart) && imageDateTime.isBefore(
                        eventEnd
                    ))
                ) {
                    val storageRef =
                        storage.getReference(event.id + "/" + image.id + ".jpg")
                    storageRef.putFile(image.uri).addOnSuccessListener {
                        Log.d("upload", "image added successfully!")
                    }
                } else {
                    Log.d("EventPage", "Photo was now taken at the event")
                    view?.let {
                        Snackbar.make(
                            it,
                            "Photo was now taken at the event",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun convertToDegree(stringDMS: String): Double? {
        val result: Double?
        val dMS = stringDMS.split(",".toRegex(), 3).toTypedArray()

        val stringD = dMS[0].split("/".toRegex(), 2).toTypedArray()
        val d0 = stringD[0].toDouble()
        val d1 = stringD[1].toDouble()
        val floatD = d0 / d1

        val stringM = dMS[1].split("/".toRegex(), 2).toTypedArray()
        val m0 = stringM[0].toDouble()
        val m1 = stringM[1].toDouble()
        val floatM = m0 / m1

        val stringS = dMS[2].split("/".toRegex(), 2).toTypedArray()
        val s0 = stringS[0].toDouble()
        val s1 = stringS[1].toDouble()
        val floatS = s0 / s1

        result = floatD + floatM / 60 + floatS / 3600

        return result
    }

    @SuppressLint("SetTextI18n")
    private fun initParticipantsList() {
        val linearLayout = LinearLayout(context)
        val linearLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.layoutParams = linearLayoutParams
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER

        for (i in 0 until event.participants.size) {
            val card = CardView(context!!)
            card.radius = 100F

            if (i == MAX_PARTICIPANTS_SHOWN) {
                val last = TextView(context)
                val participantsLeft = event.participants.size - MAX_PARTICIPANTS_SHOWN
                last.text = "+$participantsLeft"
                last.setTextColor(Color.WHITE)
                last.gravity = Gravity.CENTER
                last.background = getDrawable(context!!, R.drawable.dark_grey_color)
                last.layoutParams = ViewGroup.LayoutParams(64, 64)
                card.addView(last)
                linearLayout.addView(card)
                break
            }

            val avatar = ImageView(context)
            avatar.layoutParams = ViewGroup.LayoutParams(64, 64)
            database.collection("users").document(event.participants[i])
                .get()
                .addOnSuccessListener { document ->
                    avatar.setImageResource((document.get("avatar") as Long).toInt())
                }
            card.addView(avatar)
            linearLayout.addView(card)

        }
        event_bar.addView(linearLayout, 7)
    }

    private fun initGrid(view: View) {
        val grid = view.findViewById<RecyclerView>(R.id.images_grid)
        grid.layoutManager = GridLayoutManager(this.context, 4)
        imageAdapter = ImageAdapter(images, this)
        grid.adapter = imageAdapter
        grid.addItemDecoration(GridItemDecorator(4))

        images.clear()

        val storageRef = storage.reference
        val eventFolder = storageRef.child(event.id)
        eventFolder.listAll().addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { url ->
                        images.add(Image(url))
                        Log.e("images", url.toString())
                        imageAdapter.notifyItemInserted(images.size - 1)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("images", "failed to get images from storage")
            }
    }


    fun setSelectLayout() {
        view!!.findViewById<ImageView>(R.id.event_add).visibility = View.INVISIBLE
        view!!.findViewById<ImageView>(R.id.event_more).visibility = View.INVISIBLE
        view!!.findViewById<ImageView>(R.id.event_download).visibility = View.VISIBLE
        if (isAdmin)
            view!!.findViewById<ImageView>(R.id.event_delete).visibility = View.VISIBLE
    }

    fun setRegularLayout() {
        view!!.findViewById<ImageView>(R.id.event_add).visibility = View.VISIBLE
        view!!.findViewById<ImageView>(R.id.event_more).visibility = View.VISIBLE
        view!!.findViewById<ImageView>(R.id.event_download).visibility = View.INVISIBLE
        if (isAdmin)
            view!!.findViewById<ImageView>(R.id.event_delete).visibility = View.INVISIBLE
    }


    class GridItemDecorator(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view) // item position
            val column: Int = position % 4 // item column
            outRect.left = spacing - column * spacing / 4
            outRect.right = (column + 1) * spacing / 4
            if (position < 4) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        }
    }

    fun openImageViewer(position: Int) {
        val imageViewerFragment = ImageViewer(images, position)
        val transaction = fragmentManager!!.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction
            .add(android.R.id.content, imageViewerFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteImage(uri: Uri) {
        val storageReference: StorageReference =
            storage.getReferenceFromUrl(uri.toString())
        Log.e("delete", event.id + "/" + id + ".jpg")
        storageReference.delete().addOnSuccessListener {
            Log.d("upload", "image deleted successfully")
            view?.let { view ->
                Snackbar.make(
                    view,
                    "Images deleted successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener {
            Log.d("upload", "image delete error")
        }
    }

    override fun customOnBackPressed(): Boolean {
        // unselect all and exit select mode
        return if (imageAdapter.mode == ImageAdapter.MODE.SELECT) {
            Log.e("back", "Select mode is on")
            for (image in images) {
                image.unselect()
            }
            imageAdapter.notifyDataSetChanged()
            imageAdapter.mode = ImageAdapter.MODE.REGULAR
            true
        } else {
            false
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 33
        private const val IMAGE_PICK_CODE = 1000
        private const val MAX_PARTICIPANTS_SHOWN = 3
    }

}
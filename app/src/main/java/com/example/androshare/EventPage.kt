package com.example.androshare

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_event_page.*
import kotlinx.android.synthetic.main.fragment_event_page.view.*


class EventPage(private val event: Event) : Fragment() {

    private lateinit var grid: GridView
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var images: ArrayList<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = FirebaseStorage.getInstance()
        images = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.event_back).setOnClickListener {
            activity!!.onBackPressed()
        }
        view.findViewById<ImageView>(R.id.event_add).setOnClickListener {
            uploadImage()
        }
        view.findViewById<ImageView>(R.id.event_more).setOnClickListener {
            // TODO implement
            Toast.makeText(context, "More here", Toast.LENGTH_SHORT).show()
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
        initParticipantsList()
        initGrid(view)
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
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        // When an Image is picked
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK
            && null != data
        ) {
            val pickedImages = ArrayList<Uri>()
            var imageUri: Uri? = null

            if (data.clipData != null) {
                // Two or more images we picked
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    imageUri = data.clipData!!.getItemAt(i).uri
                    pickedImages.add(imageUri)
                }
            } else if (data.data != null) {
                // One image was picked
                imageUri = data.data
                pickedImages.add(imageUri!!)
            }

            for (img in pickedImages) {
                // Upload to storage
                val storageRef = storage.getReference("images/my_photo.jpg")
                storageRef.putFile(imageUri!!).addOnSuccessListener {
                    Log.d("upload", "image added successfully!")
                }
            }


            val exifInterface =
                ExifInterface(context!!.contentResolver.openInputStream(imageUri!!)!!)

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
            exif += "\n TAG_GPS_LATITUDE_REF: " + exifInterface.getAttribute(TAG_GPS_LATITUDE_REF)
            exif += "\n TAG_GPS_LONGITUDE: " + exifInterface.getAttribute(TAG_GPS_LONGITUDE)
            exif += "\n TAG_GPS_LONGITUDE_REF: " + exifInterface.getAttribute(TAG_GPS_LONGITUDE_REF)
            exif += "\n TAG_GPS_PROCESSING_METHOD: " + exifInterface.getAttribute(
                TAG_GPS_PROCESSING_METHOD
            )

            Log.e("exif", exif)
        }
    }

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
            avatar.setImageResource(event.participants[i].avatar)
            card.addView(avatar)
            linearLayout.addView(card)

        }
        event_bar.addView(linearLayout, 2)
    }

    private fun initGrid(view: View) {
        grid = view.findViewById(R.id.photo_grid)
        val storageRef = storage.reference

        // TODO get urls of all images in event
        storageRef.child("images/NYC.jpg").downloadUrl.addOnSuccessListener { url ->
            Log.d("image", "Got the download URL '$url")

            images.add(url)
            photoAdapter = PhotoAdapter(activity!!, images)
            grid.adapter = photoAdapter

        }.addOnFailureListener {
            Log.d("image", "no url")
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 33
        private const val IMAGE_PICK_CODE = 1000
        private const val MAX_PARTICIPANTS_SHOWN = 3
    }

}

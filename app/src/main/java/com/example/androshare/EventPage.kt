package com.example.androshare

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.event_back).setOnClickListener {
            activity!!.onBackPressed()
        }
        view.findViewById<ImageView>(R.id.event_add).setOnClickListener {
            Toast.makeText(context, "Add a new photo", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<ImageView>(R.id.event_more).setOnClickListener {
            Toast.makeText(context, "More here", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<LinearLayout>(R.id.event_bar).setOnClickListener {
            Toast.makeText(context, "Show info about event", Toast.LENGTH_SHORT).show()
        }
        view.event_title.text = event.title
        view.event_description.text = event.description
        initParticipantsList()
        initGrid(view)
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

        var i = 0
        while (i < event.participants.size) {
            val card = CardView(context!!)
            card.radius = 100F

            val avatar = ImageView(context)
            avatar.layoutParams = ViewGroup.LayoutParams(64, 64)
            if (i == 5) {
                avatar.setImageResource(R.drawable.ic_more_horiz_black_24dp)
                avatar.setBackgroundResource(R.color.darkGreyColor)
            } else {
                avatar.setImageResource(event.participants[i].avatar)
            }
            card.addView(avatar)
            linearLayout.addView(card)
            if (i == 5) {
                break
            }
            i++
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

}

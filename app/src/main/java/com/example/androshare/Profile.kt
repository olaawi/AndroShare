package com.example.androshare

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Profile : Fragment() {

    private var myEventsCountTextView: TextView? = null
    private var nameTextView: TextView? = null
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val acct = GoogleSignIn.getLastSignedInAccount(activity)
        myEventsCountTextView = view.findViewById<View>(R.id.profile_my_events_count) as TextView
        nameTextView = view.findViewById<View>(R.id.profile_name) as TextView
        database.collection("users").whereEqualTo("id", acct!!.id)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    nameTextView!!.text =
                        (document.get("givenName") as String) + " " + (document.get("familyName") as String)
                    val eventsCount = (document.get("events") as ArrayList<*>).size
                    myEventsCountTextView!!.text = eventsCount.toString()
                    val avatarImage = view.findViewById<ImageView>(R.id.profile_avatar)
                    avatarImage.setImageResource((document.get("avatar") as Long).toInt())
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Profile", "Error getting documents: ", exception)
            }
        val signOutButton = view.findViewById<TextView>(R.id.profile_signout)
        signOutButton.setOnClickListener {
            signOut()
        }
        val editAvatarTextView = view.findViewById<TextView>(R.id.profile_edit_avatar)
        editAvatarTextView.setOnClickListener {
            val chooseAvatarFragement = ChooseAvatar()
            val transaction = fragmentManager!!.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction
                .add(android.R.id.content, chooseAvatarFragement)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun signOut() {
        startActivity(SignInActivity.getLaunchIntent(this.context!!))
        FirebaseAuth.getInstance().signOut()
    }

    companion object {
    }
}

package com.example.androshare


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore


class ChooseAvatar : Fragment() {

    private lateinit var grid: GridView
    private lateinit var avatars: ArrayList<Int>
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseFirestore.getInstance()
        // Initialize a list of string values
        avatars = arrayListOf()
        avatars.add(R.drawable.avatar1)
        avatars.add(R.drawable.avatar2)
        avatars.add(R.drawable.avatar3)
        avatars.add(R.drawable.avatar4)
        avatars.add(R.drawable.avatar5)
        avatars.add(R.drawable.avatar6)
        avatars.add(R.drawable.avatar7)
        avatars.add(R.drawable.avatar8)
        avatars.add(R.drawable.avatar9)
        avatars.add(R.drawable.avatar10)
        avatars.add(R.drawable.avatar11)
        avatars.add(R.drawable.avatar12)
        avatars.add(R.drawable.avatar13)
        avatars.add(R.drawable.avatar14)
        avatars.add(R.drawable.avatar15)
        avatars.add(R.drawable.avatar16)
        avatars.add(R.drawable.avatar17)
        avatars.add(R.drawable.avatar18)
        avatars.add(R.drawable.avatar19)
        avatars.add(R.drawable.avatar20)
        avatars.add(R.drawable.avatar21)
        avatars.add(R.drawable.avatar22)
        avatars.add(R.drawable.avatar23)
        avatars.add(R.drawable.avatar24)
        avatars.add(R.drawable.avatar25)
        avatars.add(R.drawable.avatar26)
        avatars.add(R.drawable.avatar27)
        avatars.add(R.drawable.avatar28)
        avatars.add(R.drawable.avatar29)
        avatars.add(R.drawable.avatar30)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_avatar, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        grid = view.findViewById(R.id.choose_avatar_grid)
        val adapter = AvatarAdapter(this.context!!, R.layout.avatar_image_view, avatars)
        grid.adapter = adapter

        grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _
            ->
            val acct = GoogleSignIn.getLastSignedInAccount(activity)
            database.collection("users").document(acct!!.id!!)
                .update("avatar", this.avatars[position])
                .addOnSuccessListener {
                    Log.d("ChooseAvatar", "")
                    Snackbar.make(
                        view,
                        "Successfully updated avatar!",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Action", null)
                        .show()
                    fragmentManager!!.popBackStack()
                }
                .addOnFailureListener {
                    Log.e("ChooseAvatar", "Error updating avatar")
                }
        }
        val closeTextView = view.findViewById<ImageView>(R.id.choose_avatar_close)
        closeTextView.setOnClickListener {
            fragmentManager!!.popBackStack()
        }
    }
}

package com.example.androshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity() {

    private val signInRequest: Int = 1
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        database = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        configureGoogleSignIn()
        setupUI()
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    private fun setupUI() {
        google_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, signInRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == signInRequest) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val account = GoogleSignIn.getLastSignedInAccount(this)
                if (account != null) {
                    val id = account.id
                    var userExists = false
                    database.collection("users")
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                if (document.get("id") == id) {
                                    userExists = true
                                    break
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("signIn", "Error reading from database", exception)
                        }
                    if (!userExists) {
                        val givenName = account.givenName
                        val familyName = account.familyName
                        val email = account.email
                        val user = User(givenName!!, familyName!!, email!!, id!!)
                        database.collection("users").add(user)
                            .addOnSuccessListener { documentReference ->
                                Log.d(
                                    "signIn",
                                    "DocumentSnapshot added with ID: ${documentReference.id}"
                                )
                            }
                            .addOnFailureListener { exception ->
                                Log.w("signIn", "Error adding document", exception)
                            }
                    }
                }
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("Username","John Doe")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }
}

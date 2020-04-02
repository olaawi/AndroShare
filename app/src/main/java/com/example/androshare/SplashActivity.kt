package com.example.androshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.android.gms.auth.api.signin.GoogleSignIn

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val intent: Intent = if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                // User already signed in, go to main
                Intent(this, MainActivity::class.java)
            } else {
                // User didn't sign in, go to sign in
                Intent(this, SignInActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_DURATION)
    }

    companion object {
        const val SPLASH_DURATION: Long = 2000 // 2 sec
    }

}

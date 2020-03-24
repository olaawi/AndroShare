package com.example.androshare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({

            // TODO check if user already signed in

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }, SPLASH_DURATION)
    }

    companion object{
        const val SPLASH_DURATION: Long = 3000 // 1 sec
    }
}

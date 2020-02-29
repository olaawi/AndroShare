package com.example.androshare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var dashboardFragment: Dashboard
    private lateinit var nearMeFragment: NearMe
    private lateinit var favouritesFragment: Favourites
    private lateinit var moreFragment: More
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val acct = GoogleSignIn.getLastSignedInAccount(this) // this is how we access account in activity

        bottomNavigation = findViewById(R.id.bottom_navigation)

        dashboardFragment = Dashboard()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, dashboardFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit()

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    dashboardFragment = Dashboard()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, dashboardFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .addToBackStack(null)
                        .commit()
                }
                R.id.navigation_near_me -> {
                    nearMeFragment = NearMe()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, nearMeFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .addToBackStack(null)
                        .commit()
                }
                R.id.navigation_favourites -> {
                    favouritesFragment = Favourites()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, favouritesFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .addToBackStack(null)
                        .commit()
                }
                R.id.navigation_more -> {
                    moreFragment = More()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, moreFragment, "MORE")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .addToBackStack(null)
                        .commit()
                }

            }
            true
        }

    }
}


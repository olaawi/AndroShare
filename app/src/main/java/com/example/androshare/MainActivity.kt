package com.example.androshare

import android.os.Bundle
import android.provider.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController


class MainActivity : AppCompatActivity() {

    lateinit var dashboardFragment : Dashboard
    lateinit var nearMeFragment: NearMe
    lateinit var favouritesFragment: Favourites

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        dashboardFragment = Dashboard()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, dashboardFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
//
//        val navController = findNavController(R.id.nav_host_fragment)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_dashboard, R.id.navigation_new_event, R.id.navigation_profile, R.id.navigation_settings
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.navigation_dashboard -> {
                    dashboardFragment = Dashboard()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, dashboardFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.navigation_near_me -> {
                    nearMeFragment = NearMe()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, nearMeFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.navigation_favourites -> {
                    favouritesFragment = Favourites()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, favouritesFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }

            }
            true
        }
    }


}

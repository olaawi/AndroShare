package com.example.androshare

import android.os.Bundle
import android.provider.Settings
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController


class MainActivity : AppCompatActivity() {

    lateinit var dashboardFragment : Dashboard
    lateinit var newEventFragment : NewEvent
    lateinit var profileFragment : Profile
    lateinit var settingsFragment : AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        dashboardFragment = Dashboard()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, dashboardFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard, R.id.navigation_new_event, R.id.navigation_profile, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.navigation_dashboard -> {
                    dashboardFragment = Dashboard()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, dashboardFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.navigation_new_event -> {
                    newEventFragment = NewEvent()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, newEventFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.navigation_profile -> {
                    profileFragment = Profile()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, profileFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.navigation_settings -> {
                    settingsFragment = AppSettings()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, settingsFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
            }
            true
        }
    }


}

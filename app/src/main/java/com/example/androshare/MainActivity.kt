package com.example.androshare

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.android.synthetic.main.fragment_more.view.*


class MainActivity : AppCompatActivity() {

    private lateinit var dashboardFragment : Dashboard
    private lateinit var nearMeFragment: NearMe
    private lateinit var favouritesFragment: Favourites
    private lateinit var moreFragment : More

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val acct = GoogleSignIn.getLastSignedInAccount(this) // this is how we access account in activity

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        dashboardFragment = Dashboard()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, dashboardFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
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
                R.id.navigation_more -> {
                    moreFragment = More()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, moreFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }

            }
            true
        }
    }

//    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
//        when(menuItem.itemId){
//            R.id.appSettings -> {
//                appSettingsFragment = AppSettings()
//                supportFragmentManager
//                    .beginTransaction()
//                    .replace(R.id.frame_layout, appSettingsFragment)
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                    .commit()
//            }
//            R.id.profile -> {
//                profileFragment = Profile()
//                supportFragmentManager
//                    .beginTransaction()
//                    .replace(R.id.frame_layout, profileFragment)
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                    .commit()
//            }
//            R.id.logout -> {
//                logoutFragment = Logout()
//                supportFragmentManager
//                    .beginTransaction()
//                    .replace(R.id.frame_layout, logoutFragment)
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                    .commit()
//            }
//
//        }
//        return true
//    }

//    public fun OnBackPressed(){
//
//        if(mainLayout.isDrawerOpen(GravityCompat.START)){
//            mainLayout.closeDrawer(GravityCompat.START)
//        }
//        else{
//            super.OnBackPressed()
//        }
//    }
}


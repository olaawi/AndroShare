package com.example.androshare

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.android.synthetic.main.fragment_more.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [more.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [more.newInstance] factory method to
 * create an instance of this fragment.
 */
class More : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var appSettingsFragment: AppSettings
    private lateinit var profileFragment : Profile
    private lateinit var logoutFragment : Logout

    private var listener: Logout.OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val navigationSecondaryMenu : NavigationView = findViewById(R.id.global_settings_menu)
//        global_settings_menu.setNavigationItemSelectedListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_more, container, false)
//        val globalSettingsMenu = view.global_settings_menu
        view.global_settings_menu.setNavigationItemSelectedListener(this)
        return view
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
            R.id.navigation_app_settings -> {
                appSettingsFragment = AppSettings()
                fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,appSettingsFragment)?.commit()
            }
            R.id.navigation_profile -> {
                profileFragment = Profile()
                fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,profileFragment)?.commit()
            }
            R.id.navigation_logout -> {
                logoutFragment = Logout()
                fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,logoutFragment)?.commit()
            }

        }
        return true
    }







//    // TODO: Rename method, update argument and hook method into UI event
//    fun onButtonPressed(uri: Uri) {
//        listener?.onFragmentInteraction(uri)
//    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }

//    override fun onDetach() {
//        super.onDetach()
//        listener = null
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment more.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            More().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}

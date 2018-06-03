package org.fossasia.openevent.general

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_auth.*
import org.fossasia.openevent.general.auth.LoginFragment
import org.fossasia.openevent.general.auth.ProfileFragment
import org.fossasia.openevent.general.auth.SignUpFragment
import org.fossasia.openevent.general.event.EventsFragment

class AuthFragment : Fragment() {

    private lateinit var rootView : View
    private lateinit var bottomNavigationView: BottomNavigationView

    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.navigation_login -> {
                activity?.actionBar?.title = "Login"
                fragment = LoginFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_sign_up -> {
                activity?.actionBar?.title = "Sign Up"
                fragment = SignUpFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.actionBar?.title = "Login"

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_auth,container,false)
        bottomNavigationView = rootView.findViewById(R.id.navigation_auth)
        bottomNavigationView.setOnNavigationItemSelectedListener(listener)
        return rootView


    }

    private fun loadFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frame_container_auth, fragment)
                ?.commit()
    }

}
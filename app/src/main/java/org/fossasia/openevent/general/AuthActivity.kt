package org.fossasia.openevent.general

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_auth.*
import org.fossasia.openevent.general.auth.LoginFragment
import org.fossasia.openevent.general.auth.ProfileFragment
import org.fossasia.openevent.general.auth.SignUpFragment
import org.fossasia.openevent.general.event.EventsFragment

class AuthActivity : AppCompatActivity() {

    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.navigationLogin -> {
                supportActionBar?.title = "Login"
                fragment = LoginFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigationSignUp -> {
                supportActionBar?.title = "Sign Up"
                fragment = SignUpFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        navigationAuth.setOnNavigationItemSelectedListener(listener)

        supportActionBar?.title = "Login"

        loadFragment(LoginFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.frameContainerAuth, fragment)
                .commit()
    }
}
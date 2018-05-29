package org.fossasia.openevent.general

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.fossasia.openevent.general.utils.ConstantStrings
import org.fossasia.openevent.general.utils.SharedPreferencesUtil

class MainActivity : AppCompatActivity() {

    private var token: String? = null

    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.navigation_events -> {
                supportActionBar?.title = "Events"
                fragment = EventsFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                if (token != null) {
                    supportActionBar?.title = "Profile"
                    fragment = ProfileFragment()
                    loadFragment(fragment)
                } else {
                    Toast.makeText(applicationContext, "You need to login first!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(listener)

        supportActionBar?.title = "Events"
        token = SharedPreferencesUtil.getString(ConstantStrings.TOKEN, null)

        loadFragment(EventsFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
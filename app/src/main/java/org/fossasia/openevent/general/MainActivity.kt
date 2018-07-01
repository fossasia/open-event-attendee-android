package org.fossasia.openevent.general

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_main.*
import org.fossasia.openevent.general.auth.ProfileFragment
import org.fossasia.openevent.general.event.EventsFragment
import org.fossasia.openevent.general.favorite.FavoriteFragment
import org.fossasia.openevent.general.search.SearchFragment

class MainActivity : AppCompatActivity() {

    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.navigationEvents -> {
                supportActionBar?.title = "Events"
                fragment = EventsFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigationSearch -> {
                supportActionBar?.title = "Search"
                fragment = SearchFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigationProfile -> {
                supportActionBar?.title = "Profile"
                fragment = ProfileFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigationFavorite -> {
                supportActionBar?.title = "Likes"
                fragment = FavoriteFragment()
                loadFragment(fragment)
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

        if (savedInstanceState == null)
            loadFragment(EventsFragment())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.profile, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.setGroupVisible(R.id.profile_menu, false)
        menu?.setGroupVisible(R.id.search_menu, false)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit()
    }
}
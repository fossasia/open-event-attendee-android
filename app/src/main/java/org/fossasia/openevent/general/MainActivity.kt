package org.fossasia.openevent.general

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_main.*
import org.fossasia.openevent.general.R.id.navigation_search
import org.fossasia.openevent.general.attendees.AttendeeFragment
import org.fossasia.openevent.general.auth.LAUNCH_ATTENDEE
import org.fossasia.openevent.general.auth.ProfileFragment
import org.fossasia.openevent.general.event.EventsFragment
import org.fossasia.openevent.general.favorite.FavoriteFragment
import org.fossasia.openevent.general.order.LAUNCH_TICKETS
import org.fossasia.openevent.general.order.OrdersUnderUserFragment
import org.fossasia.openevent.general.order.TICKETS
import org.fossasia.openevent.general.search.SearchFragment
import timber.log.Timber

private const val TO_SEARCH: String = "ToSearchFragment"

class MainActivity : AppCompatActivity() {

    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.navigation_events -> {
                supportActionBar?.title = "Events"
                fragment = EventsFragment()
                checkAndLoadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                supportActionBar?.title = "Search"
                fragment = SearchFragment()
                checkAndLoadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                supportActionBar?.title = "Profile"
                fragment = ProfileFragment()
                checkAndLoadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                supportActionBar?.title = "Likes"
                fragment = FavoriteFragment()
                checkAndLoadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tickets -> {
                supportActionBar?.title = "Tickets"
                fragment = OrdersUnderUserFragment()
                checkAndLoadFragment(fragment)
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

        val bundle = intent.extras
        var openEventsFragment = true

        if (bundle != null && bundle.getBoolean(TO_SEARCH)) {
            loadFragment(SearchFragment())
            supportActionBar?.title = "Search"
            navigation.selectedItemId = navigation_search
            openEventsFragment = false
        }

        if (bundle != null && bundle.getBoolean(LAUNCH_ATTENDEE)) {
            val fragment = AttendeeFragment()
            fragment.arguments = bundle
            loadFragment(fragment)
            openEventsFragment = false
        }

        if (bundle != null && (bundle.getBoolean(TICKETS) || bundle.getBoolean(LAUNCH_TICKETS))) {
            loadFragment(OrdersUnderUserFragment())
            supportActionBar?.title = "Tickets"
            navigation.selectedItemId = R.id.navigation_tickets
            openEventsFragment = false
        }

        if (savedInstanceState == null && openEventsFragment)
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

    private fun checkAndLoadFragment(fragment: Fragment) {
        val savedFragment = supportFragmentManager.findFragmentByTag(fragment::class.java.name)
        if (savedFragment != null) {
            loadFragment(savedFragment)
            Timber.d("""Loading fragment from stack ${fragment::class.java}""")
        } else {
            loadFragment(fragment)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, fragment, fragment::class.java.name)
                .addToBackStack(null)
                .commit()
    }
}
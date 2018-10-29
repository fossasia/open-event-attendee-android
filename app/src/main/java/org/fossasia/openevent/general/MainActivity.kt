package org.fossasia.openevent.general

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_main.*
import org.fossasia.openevent.general.R.id.navigation_events
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
import org.fossasia.openevent.general.utils.Utils

private const val TO_SEARCH: String = "ToSearchFragment"

class MainActivity : AppCompatActivity() {

    val containerID: Int = R.id.frameContainer
    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.navigation_events -> {
                supportActionBar?.title = "Events"
                fragment = EventsFragment()
                Utils.checkAndLoadFragment(fragment, supportFragmentManager, containerID)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                supportActionBar?.title = "Search"
                fragment = SearchFragment()
                Utils.checkAndLoadFragment(fragment, supportFragmentManager, containerID)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                supportActionBar?.title = "Profile"
                fragment = ProfileFragment()
                Utils.checkAndLoadFragment(fragment, supportFragmentManager, containerID)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                supportActionBar?.title = "Likes"
                fragment = FavoriteFragment()
                Utils.checkAndLoadFragment(fragment, supportFragmentManager, containerID)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tickets -> {
                supportActionBar?.title = "Tickets"
                fragment = OrdersUnderUserFragment()
                Utils.checkAndLoadFragment(fragment, supportFragmentManager, containerID)
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
        Utils.bundle = bundle

        if (bundle != null && bundle.getBoolean(TO_SEARCH)) {
            Utils.loadFragment(SearchFragment(), supportFragmentManager, containerID)
            supportActionBar?.title = "Search"
            navigation.selectedItemId = navigation_search
            openEventsFragment = false
        }

        if (bundle != null && bundle.getBoolean(LAUNCH_ATTENDEE)) {
            val fragment = AttendeeFragment()
            Utils.loadFragment(fragment, supportFragmentManager, containerID)
            openEventsFragment = false
        }

        if (bundle != null && (bundle.getBoolean(TICKETS) || bundle.getBoolean(LAUNCH_TICKETS))) {
            Utils.loadFragment(OrdersUnderUserFragment(), supportFragmentManager, containerID)
            supportActionBar?.title = "Tickets"
            navigation.selectedItemId = R.id.navigation_tickets
            openEventsFragment = false
        }

        if (savedInstanceState == null && openEventsFragment)
            Utils.loadFragment(EventsFragment(), supportFragmentManager, containerID)
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

    override fun onBackPressed() {
        val currentFragment = this.supportFragmentManager.findFragmentById(R.id.frameContainer)
        if (currentFragment !is EventsFragment) {
            Utils.loadFragment(EventsFragment(), supportFragmentManager, containerID)
            navigation.selectedItemId = navigation_events
        } else {
            super.onBackPressed()
        }
    }
}
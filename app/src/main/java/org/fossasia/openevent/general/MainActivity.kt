package org.fossasia.openevent.general

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.navigation
import org.fossasia.openevent.general.order.LAUNCH_TICKETS
import org.fossasia.openevent.general.order.TICKETS

private const val TO_SEARCH: String = "ToSearchFragment"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hostFragment = supportFragmentManager.findFragmentById(R.id.frameContainer)
        var navController: NavController? = null
        if (hostFragment is NavHostFragment) {
            navController = hostFragment.navController
            setupBottomNavigationMenu(navController)
        }

        val bundle = if (savedInstanceState == null) intent.extras else null
        if (bundle != null) {
            if (bundle.getBoolean(TO_SEARCH))
                navController?.navigate(R.id.navigation_search)

            if (bundle.getBoolean(TICKETS) || bundle.getBoolean(LAUNCH_TICKETS))
                navController?.navigate(R.id.navigation_tickets)
        }

        navController?.addOnDestinationChangedListener { controller, destination, arguments ->
            handleNavigationVisibility(destination.id)
        }
    }

    private fun setupBottomNavigationMenu(navController: NavController) {
        setupWithNavController(navigation, navController)
    }

    private fun handleNavigationVisibility(id: Int) {
        navigation.visibility =
            when (id) {
                R.id.navigation_events,
                R.id.navigation_search,
                R.id.navigation_profile,
                R.id.navigation_tickets,
                R.id.navigation_favorite -> View.VISIBLE
                else -> View.GONE
        }
    }
}

package org.fossasia.openevent.general

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.navigation
import kotlinx.android.synthetic.main.activity_main.navigationAuth
import org.fossasia.openevent.general.order.LAUNCH_TICKETS
import org.fossasia.openevent.general.order.TICKETS
import org.fossasia.openevent.general.search.TO_SEARCH

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var currentFragmentId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hostFragment = supportFragmentManager.findFragmentById(R.id.frameContainer)
        if (hostFragment is NavHostFragment)
            navController = hostFragment.navController
        setupBottomNavigationMenu(navController)

        val bundle = if (savedInstanceState == null) intent.extras else null
        if (bundle != null) {
            if (bundle.getBoolean(TO_SEARCH))
                navController.navigate(R.id.searchFragment)

            if (bundle.getBoolean(TICKETS) || bundle.getBoolean(LAUNCH_TICKETS))
                navController.navigate(R.id.orderUnderUserFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragmentId = destination.id
            handleNavigationVisibility(currentFragmentId)
        }
    }

    private fun setupBottomNavigationMenu(navController: NavController) {
        setupWithNavController(navigation, navController)
        setupWithNavController(navigationAuth, navController)
    }

    private fun handleNavigationVisibility(id: Int) {
        navigation.visibility =
            when (id) {
                R.id.eventsFragment,
                R.id.searchFragment,
                R.id.profileFragment,
                R.id.orderUnderUserFragment,
                R.id.favoriteFragment -> View.VISIBLE
                else -> View.GONE
        }
        navigationAuth.visibility =
            when (id) {
                R.id.loginFragment,
                R.id.signUpFragment -> View.VISIBLE
                else -> View.GONE
            }
    }

    override fun onBackPressed() {
        when (currentFragmentId) {
            R.id.loginFragment,
            R.id.signUpFragment -> {
                navController.popBackStack(R.id.eventsFragment, false)
                Snackbar.make(window.decorView, "Sign in canceled!", Snackbar.LENGTH_SHORT).show()
            }
            else -> super.onBackPressed()
        }
    }
}

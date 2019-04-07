package org.fossasia.openevent.general

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.navigation
import kotlinx.android.synthetic.main.activity_main.navigationAuth
import kotlinx.android.synthetic.main.activity_main.mainFragmentCoordinatorLayout
import org.fossasia.openevent.general.auth.EditProfileFragment
import org.fossasia.openevent.general.utils.Utils.navAnimGone
import org.fossasia.openevent.general.utils.Utils.navAnimVisible

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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragmentId = destination.id
            handleNavigationVisibility(currentFragmentId)
        }
    }

    private fun setupBottomNavigationMenu(navController: NavController) {
        setupWithNavController(navigation, navController)
        setupWithNavController(navigationAuth, navController)

        navigation.setOnNavigationItemReselectedListener {
            val hostFragment = supportFragmentManager.findFragmentById(R.id.frameContainer)
            if (hostFragment is NavHostFragment) {
                val currentFragment = hostFragment.childFragmentManager.fragments.first()
                if (currentFragment is ScrollToTop) currentFragment.scrollToTop()
            }
        }
    }

    private fun handleNavigationVisibility(id: Int) {
        when (id) {
            R.id.eventsFragment,
            R.id.searchFragment,
            R.id.profileFragment,
            R.id.orderUnderUserFragment,
            R.id.favoriteFragment -> navAnimVisible(navigation, this@MainActivity)
            else -> navAnimGone(navigation, this@MainActivity)
        }
        when (id) {
            R.id.loginFragment,
            R.id.signUpFragment -> navAnimVisible(navigationAuth, this@MainActivity)
            else -> navAnimGone(navigationAuth, this@MainActivity)
        }
    }

    override fun onBackPressed() {
        when (currentFragmentId) {
            R.id.loginFragment,
            R.id.signUpFragment -> {
                navController.popBackStack(R.id.eventsFragment, false)
                Snackbar.make(
                    mainFragmentCoordinatorLayout, R.string.sign_in_canceled, Snackbar.LENGTH_SHORT
                ).show()
            }
            R.id.orderCompletedFragment -> navController.popBackStack(R.id.eventDetailsFragment, false)
            R.id.welcomeFragment -> finish()
            R.id.editProfileFragment -> {

                // Calls the handleBackPress method in EditProfileFragment
                val hostFragment = supportFragmentManager.findFragmentById(R.id.frameContainer) as? NavHostFragment
                (hostFragment?.childFragmentManager?.fragments?.get(0) as? EditProfileFragment)?.handleBackPress()
            }
            else -> super.onBackPressed()
        }
    }

    /**
     * Called by EditProfileFragment to go to previous fragment
     */
    fun onSuperBackPressed() {
        super.onBackPressed()
    }
}

interface ScrollToTop {
    fun scrollToTop()
}

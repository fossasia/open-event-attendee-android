package org.fossasia.openevent.general

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.navigation
import kotlinx.android.synthetic.main.activity_main.navigationAuth
import kotlinx.android.synthetic.main.activity_main.mainFragmentCoordinatorLayout
import org.fossasia.openevent.general.auth.EditProfileFragment
import org.fossasia.openevent.general.search.RC_CREDENTIALS_READ
import org.fossasia.openevent.general.search.RC_CREDENTIALS_SAVE
import org.fossasia.openevent.general.search.SmartAuthViewModel
import org.fossasia.openevent.general.utils.Utils.navAnimGone
import org.fossasia.openevent.general.utils.Utils.navAnimVisible
import org.fossasia.openevent.general.utils.extensions.setupWithNavController

class MainActivity : AppCompatActivity() {
    private var navController: LiveData<NavController>? = null
    private var currentFragmentId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            setupBottomNavigationMenu()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationMenu()
    }

    private fun setupBottomNavigationMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        val navGraphIds = listOf(R.navigation.events, R.navigation.search,
            R.navigation.likes, R.navigation.tickets, R.navigation.profile)
        val controller: LiveData<NavController> = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.frameContainer,
            intent = intent
        )
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
            navController.addOnDestinationChangedListener { _, des, _ ->
                currentFragmentId = des.id
                handleNavigationVisibility(currentFragmentId)
            }
            setupWithNavController(navigationAuth, navController)
        })

        navController = controller
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
                findViewById<BottomNavigationView>(R.id.navigation).selectedItemId = R.id.events
                Snackbar.make(
                    mainFragmentCoordinatorLayout, R.string.sign_in_canceled, Snackbar.LENGTH_SHORT
                ).show()
            }
            R.id.orderCompletedFragment -> {
                navController?.value?.popBackStack(R.id.eventDetailsFragment, false)
                val navigation = findViewById<BottomNavigationView>(R.id.navigation)
                val currentNavController = navController?.value
                currentNavController?.popBackStack(currentNavController.graph.startDestination, false)
                navigation?.selectedItemId = R.id.events
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_CREDENTIALS_READ || requestCode == RC_CREDENTIALS_SAVE)
            SmartAuthViewModel().onActivityResult(requestCode, resultCode, data, this)
        else
            super.onActivityResult(requestCode, resultCode, data)
    }
}

interface ScrollToTop {
    fun scrollToTop()
}

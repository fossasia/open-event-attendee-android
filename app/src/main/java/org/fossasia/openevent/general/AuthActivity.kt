package org.fossasia.openevent.general

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_auth.*
import org.fossasia.openevent.general.auth.LoginFragment
import org.fossasia.openevent.general.auth.SignUpFragment
import org.fossasia.openevent.general.utils.Utils

class AuthActivity : AppCompatActivity() {

    val manager:FragmentManager = supportFragmentManager
    val containerID:Int = R.id.frameContainerAuth
    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.navigation_login -> {
                supportActionBar?.title = "Login"
                fragment = LoginFragment()
                Utils.checkAndLoadFragment(fragment,manager,containerID)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_sign_up -> {
                supportActionBar?.title = "Sign Up"
                fragment = SignUpFragment()
                Utils.checkAndLoadFragment(fragment,manager,containerID)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        if (this.intent.extras != null) {
            Utils.bundle = this.intent.extras
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationAuth.setOnNavigationItemSelectedListener(listener)

        supportActionBar?.title = "Login"

        Utils.checkAndLoadFragment(LoginFragment(),manager,containerID)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }
}
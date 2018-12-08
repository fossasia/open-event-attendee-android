package org.fossasia.openevent.general

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_auth.*
import org.fossasia.openevent.general.auth.LoginFragment
import org.fossasia.openevent.general.auth.SignUpFragment
import org.fossasia.openevent.general.utils.Utils.loadFragment

class AuthActivity : AppCompatActivity() {

    private var bundle: Bundle? = null
    private val listener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.navigation_login -> {
                supportActionBar?.title = "Login"
                fragment = LoginFragment()
                setFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_sign_up -> {
                supportActionBar?.title = "Sign Up"
                fragment = SignUpFragment()
                setFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        if (this.intent.extras != null) {
            bundle = this.intent.extras
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationAuth.setOnNavigationItemSelectedListener(listener)

        supportActionBar?.title = "Login"

        setFragment(LoginFragment())
    }

    private fun setFragment(fragment: Fragment) {
        if (bundle != null)
            fragment.arguments = bundle
        loadFragment(supportFragmentManager, fragment, frameContainerAuth.id, false)
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

package org.fossasia.openevent.general.auth

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private val loginActivityViewModel: LoginActivityViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (loginActivityViewModel.isLoggedIn())
            redirectToMain()

        setContentView(R.layout.activity_login)

        loginButton.setOnClickListener {
            loginActivityViewModel.login(username.text.toString(), password.text.toString())
        }

        loginActivityViewModel.getProgress().observe(this, Observer {
            it?.let { showProgress(it) }
        })

        loginActivityViewModel.getError().observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        loginActivityViewModel.getLoggedIn().observe(this, Observer {
            Toast.makeText(applicationContext, "Success!", Toast.LENGTH_LONG).show()
            redirectToMain()
        })

    }

    private fun redirectToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showProgress(enabled: Boolean) {
        loginButton.isEnabled = !enabled
        progressBar.visibility = if (enabled) View.VISIBLE else View.GONE
    }
}
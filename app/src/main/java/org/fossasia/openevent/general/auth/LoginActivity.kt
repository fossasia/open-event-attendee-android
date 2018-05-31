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
import org.koin.android.architecture.ext.viewModel

class LoginActivity : AppCompatActivity() {

    private val loginActivityViewModel by viewModel<LoginActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (loginActivityViewModel.isLoggedIn())
            redirectToMain()

        setContentView(R.layout.activity_login)

        loginButton.setOnClickListener {
            if (username.text.toString().isEmpty() || password.text.toString().isEmpty()) {
                Toast.makeText(this, "Username or Password can not be empty!", Toast.LENGTH_SHORT).show()
            } else {
                loginActivityViewModel.login(username.text.toString(), password.text.toString())
            }
        }

        loginActivityViewModel.progress.observe(this, Observer {
            it?.let { showProgress(it) }
        })

        loginActivityViewModel.error.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        loginActivityViewModel.loggedIn.observe(this, Observer {
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
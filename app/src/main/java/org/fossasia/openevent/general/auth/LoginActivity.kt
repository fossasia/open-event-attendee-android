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
        supportActionBar?.title = "Login"

        loginButton.setOnClickListener {
            loginActivityViewModel.login(username.text.toString(), password.text.toString())
        }

        signupLink.setOnClickListener {
            openSignUp()
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

    private fun openSignUp() {
        val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showProgress(enabled: Boolean) {
        loginButton.isEnabled = !enabled
        progressBar.visibility = if (enabled) View.VISIBLE else View.GONE
    }
}
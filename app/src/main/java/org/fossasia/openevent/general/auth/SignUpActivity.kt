package org.fossasia.openevent.general.auth

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_signup.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel

class SignUpActivity : AppCompatActivity() {

    private val signUpActivityViewModel by viewModel<SignUpActivityViewModel>()
    private lateinit var username: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_signup)
        supportActionBar?.title = "Sign Up"

        signUpButton.setOnClickListener {
            username = usernameSignUp.text.toString()
            password = passwordSignUp.text.toString()
            signUpActivityViewModel.signUp(username, password)
        }

        signUpActivityViewModel.progress.observe(this, Observer {
            it?.let { showProgress(it) }
        })

        signUpActivityViewModel.error.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        signUpActivityViewModel.signedUp.observe(this, Observer {
            Toast.makeText(applicationContext, "Sign Up Success!", Toast.LENGTH_LONG).show()
            signUpActivityViewModel.loginAfterSignUp(username, password)
        })

        signUpActivityViewModel.loggedIn.observe(this, Observer {
            Toast.makeText(applicationContext, "Logged in Automatically!", Toast.LENGTH_LONG).show()
            redirectToMain()
        })
    }

    private fun showProgress(enabled: Boolean) {
        signUpButton.isEnabled = !enabled
        progressBarSignUp.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun redirectToMain() {
        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
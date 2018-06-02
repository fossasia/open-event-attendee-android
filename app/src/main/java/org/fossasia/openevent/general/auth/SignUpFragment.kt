package org.fossasia.openevent.general.auth

import android.support.v4.app.Fragment
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.fragment_signup.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel

class SignUpFragment : Fragment() {

    private val signUpActivityViewModel by viewModel<SignUpFragmentViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_signup, container, false)
        lateinit var emailLogin: String
        lateinit var passwordLogin: String
        lateinit var confirmPassword: String

        setHasOptionsMenu(true)

        rootView.signUpButton.setOnClickListener {
            val signUp = SignUp()
            signUp.email = usernameSignUp.text.toString()
            emailLogin = signUp.email.toString()
            signUp.password = passwordSignUp.text.toString()
            passwordLogin = signUp.password.toString()
            signUp.firstName = firstNameText.text.toString()
            signUp.lastName = lastNameText.text.toString()
            confirmPassword = confirmPasswords.text.toString()

            signUpActivityViewModel.signUp(signUp, confirmPassword)
        }

        signUpActivityViewModel.progress.observe(this, Observer {
            it?.let { showProgress(it) }
        })

        signUpActivityViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        signUpActivityViewModel.signedUp.observe(this, Observer {
            Toast.makeText(context, "Sign Up Success!", Toast.LENGTH_LONG).show()
            signUpActivityViewModel.login(emailLogin, passwordLogin)
        })

        signUpActivityViewModel.loggedIn.observe(this, Observer {
            Toast.makeText(context, "Logged in Automatically!", Toast.LENGTH_LONG).show()
            redirectToMain()
        })

        return rootView
    }

    private fun showProgress(enabled: Boolean) {
        signUpButton.isEnabled = !enabled
        progressBarSignUp.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun redirectToMain() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

}
package org.fossasia.openevent.general.auth

import android.support.v4.app.Fragment
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.fragment_signup.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel

class SignUpFragment : Fragment() {

    private val signUpActivityViewModel by viewModel<SignUpFragmentViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_signup, container, false)
        lateinit var confirmPassword: String

        setHasOptionsMenu(true)
        val signUp = SignUp()

        rootView.signUpButton.setOnClickListener {
            signUp.email = usernameSignUp.text.toString()
            signUp.password = passwordSignUp.text.toString()
            signUp.firstName = firstNameText.text.toString()
            signUp.lastName = lastNameText.text.toString()
            confirmPassword = confirmPasswords.text.toString()
            signUpActivityViewModel.signUp(signUp, confirmPassword)
        }

        signUpActivityViewModel.progress.observe(this, Observer {
            it?.let {
                Utils.showProgressBar(rootView.progressBarSignUp, it)
                signUpButton.isEnabled = !it
            }
        })

        signUpActivityViewModel.showNoInternetDialog.observe(this, Observer {
            Utils.showNoInternetDialog(context)
        })

        signUpActivityViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        signUpActivityViewModel.signedUp.observe(this, Observer {
            Toast.makeText(context, "Sign Up Success!", Toast.LENGTH_LONG).show()
            signUpActivityViewModel.login(signUp)
        })

        signUpActivityViewModel.loggedIn.observe(this, Observer {
            Toast.makeText(context, "Logged in Automatically!", Toast.LENGTH_LONG).show()
            redirectToMain()
        })

        rootView.passwordSignUp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (passwordSignUp.text.toString().length >= 6 || passwordSignUp.text.toString().isEmpty()) {
                    textInputLayoutPassword.error = null
                    textInputLayoutPassword.isErrorEnabled = false
                } else {
                    textInputLayoutPassword.error = "Password too short!"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })

        return rootView
    }

    private fun redirectToMain() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        activity?.finish()
    }
}

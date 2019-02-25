package org.fossasia.openevent.general.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_signup.confirmPasswords
import kotlinx.android.synthetic.main.fragment_signup.firstNameText
import kotlinx.android.synthetic.main.fragment_signup.lastNameText
import kotlinx.android.synthetic.main.fragment_signup.passwordSignUp
import kotlinx.android.synthetic.main.fragment_signup.textInputLayoutPassword
import kotlinx.android.synthetic.main.fragment_signup.usernameSignUp
import kotlinx.android.synthetic.main.fragment_signup.signUpButton
import kotlinx.android.synthetic.main.fragment_signup.textInputLayoutConfirmPassword
import kotlinx.android.synthetic.main.fragment_signup.view.signUpButton
import kotlinx.android.synthetic.main.fragment_signup.view.lastNameText
import kotlinx.android.synthetic.main.fragment_signup.view.passwordSignUp
import kotlinx.android.synthetic.main.fragment_signup.view.confirmPasswords
import kotlinx.android.synthetic.main.fragment_signup.view.usernameSignUp
import kotlinx.android.synthetic.main.fragment_signup.view.signupNestedScrollView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpFragment : Fragment() {

    private val signUpViewModel by viewModel<SignUpViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_signup, container, false)

        val progressDialog = Utils.progressDialog(context)
        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = "Sign Up"
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        lateinit var confirmPassword: String
        val signUp = SignUp()

        rootView.lastNameText.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    rootView.signUpButton.performClick()
                    Utils.hideSoftKeyboard(context, rootView)
                    true
                }
                else -> false
            }
        }

        rootView.signUpButton.setOnClickListener {
            signUp.email = usernameSignUp.text.toString()
            signUp.password = passwordSignUp.text.toString()
            signUp.firstName = firstNameText.text.toString()
            signUp.lastName = lastNameText.text.toString()
            confirmPassword = confirmPasswords.text.toString()
            signUpViewModel.signUp(signUp, confirmPassword)
        }

        signUpViewModel.progress
            .nonNull()
            .observe(this, Observer {
                progressDialog.show(it)
            })

        signUpViewModel.showNoInternetDialog
            .nonNull()
            .observe(this, Observer {
                Utils.showNoInternetDialog(context)
            })

        signUpViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(rootView.signupNestedScrollView, it, Snackbar.LENGTH_LONG).show()
            })

        signUpViewModel.signedUp
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(
                    rootView.signupNestedScrollView, R.string.sign_up_success, Snackbar.LENGTH_SHORT
                ).show()
                signUpViewModel.login(signUp)
            })

        signUpViewModel.loggedIn
            .nonNull()
            .observe(this, Observer {
                redirectToMain()
            })

        signUpViewModel.areFieldsCorrect
            .nonNull()
            .observe(this, Observer {
                signUpButton.isEnabled = it
            })

        rootView.usernameSignUp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { /*Implement here*/ }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpViewModel.checkFields(
                    usernameSignUp.text.toString(), passwordSignUp.text.toString(), confirmPasswords.text.toString())
            }
        })

        rootView.confirmPasswords.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (confirmPasswords.text.toString().equals(passwordSignUp.text.toString())) {
                    textInputLayoutConfirmPassword.error = null
                    textInputLayoutConfirmPassword.isErrorEnabled = false
                } else {
                    textInputLayoutConfirmPassword.error = "Your password and confirmation password do not match!"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpViewModel.checkFields(
                    usernameSignUp.text.toString(), passwordSignUp.text.toString(), confirmPasswords.text.toString())
            }
        })

        rootView.passwordSignUp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (passwordSignUp.text.toString().length >= 6 || passwordSignUp.text.toString().isEmpty()) {
                    textInputLayoutPassword.error = null
                    textInputLayoutPassword.isErrorEnabled = false
                } else {
                    textInputLayoutPassword.error = "Password too short!"
                }
                if (confirmPasswords.text.toString().equals(passwordSignUp.text.toString())) {
                    textInputLayoutConfirmPassword.error = null
                    textInputLayoutConfirmPassword.isErrorEnabled = false
                } else {
                    textInputLayoutConfirmPassword.error = "Your password and confirmation password do not match!"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpViewModel.checkFields(
                    usernameSignUp.text.toString(), passwordSignUp.text.toString(), confirmPasswords.text.toString())
            }
        })

        return rootView
    }

    private fun redirectToMain() {
        findNavController(rootView).popBackStack()
        Snackbar.make(rootView, R.string.logged_in_automatically, Snackbar.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController(rootView).popBackStack(R.id.eventsFragment, false)
                Snackbar.make(rootView, R.string.sign_in_canceled, Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

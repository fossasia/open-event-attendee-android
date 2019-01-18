package org.fossasia.openevent.general.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login.email
import kotlinx.android.synthetic.main.fragment_login.loginButton
import kotlinx.android.synthetic.main.fragment_login.password
import kotlinx.android.synthetic.main.fragment_login.view.email
import kotlinx.android.synthetic.main.fragment_login.view.loginCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_login.view.forgotPassword
import kotlinx.android.synthetic.main.fragment_login.view.loginButton
import kotlinx.android.synthetic.main.fragment_login.view.loginLayout
import kotlinx.android.synthetic.main.fragment_login.view.progressBar
import kotlinx.android.synthetic.main.fragment_login.view.sentEmailLayout
import kotlinx.android.synthetic.main.fragment_login.view.tick
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

const val SNACKBAR_MESSAGE: String = "SNACKBAR_MESSAGE"

class LoginFragment : Fragment() {

    private val loginViewModel by viewModel<LoginViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_login, container, false)

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = "Login"
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        showSnackbar()

        if (loginViewModel.isLoggedIn())
            redirectToMain()

        rootView.loginButton.setOnClickListener {
            loginViewModel.login(email.text.toString(), password.text.toString())
            hideSoftKeyboard(context, rootView)
        }

        loginViewModel.progress
            .nonNull()
            .observe(this, Observer {
                rootView.progressBar.isVisible = it
                loginButton.isEnabled = !it
            })

        loginViewModel.showNoInternetDialog
            .nonNull()
            .observe(this, Observer {
                Utils.showNoInternetDialog(context)
            })

        loginViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(rootView.loginCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        loginViewModel.loggedIn
            .nonNull()
            .observe(this, Observer {
                loginViewModel.fetchProfile()
            })

        rootView.email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(email: CharSequence, start: Int, before: Int, count: Int) {
                loginViewModel.checkEmail(email.toString())
            }
        })

        loginViewModel.requestTokenSuccess
            .nonNull()
            .observe(this, Observer {
                rootView.sentEmailLayout.visibility = View.VISIBLE
                rootView.loginLayout.visibility = View.GONE
            })

        loginViewModel.isCorrectEmail
            .nonNull()
            .observe(this, Observer {
                onEmailEntered(it)
            })

        rootView.tick.setOnClickListener {
            rootView.sentEmailLayout.visibility = View.GONE
            rootView.loginLayout.visibility = View.VISIBLE
        }

        rootView.forgotPassword.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            loginViewModel.sendResetPasswordEmail(email.text.toString())
        }

        loginViewModel.user
            .nonNull()
            .observe(this, Observer {
                redirectToMain()
            })

        return rootView
    }

    private fun redirectToMain() {
        findNavController(rootView).popBackStack()
        Snackbar.make(rootView, R.string.welcome_back, Snackbar.LENGTH_SHORT).show()
    }

    private fun onEmailEntered(enable: Boolean) {
        if (!enable) rootView.email.error = getString(R.string.email_error) else rootView.email.error = null
        rootView.loginButton.isEnabled = enable
        rootView.forgotPassword.isVisible = enable
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

    private fun showSnackbar() {
        val textSnackbar = arguments?.getString(SNACKBAR_MESSAGE)
        if (textSnackbar != null) {
            Snackbar.make(
                rootView.loginCoordinatorLayout, textSnackbar, Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}

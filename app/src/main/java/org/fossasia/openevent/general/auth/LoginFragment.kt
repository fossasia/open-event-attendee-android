package org.fossasia.openevent.general.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.navigationAuth
import kotlinx.android.synthetic.main.fragment_login.email
import kotlinx.android.synthetic.main.fragment_login.password
import kotlinx.android.synthetic.main.fragment_login.loginButton
import kotlinx.android.synthetic.main.fragment_login.view.password
import kotlinx.android.synthetic.main.fragment_login.view.email
import kotlinx.android.synthetic.main.fragment_login.view.loginCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_login.view.forgotPassword
import kotlinx.android.synthetic.main.fragment_login.view.loginButton
import kotlinx.android.synthetic.main.fragment_login.view.loginLayout
import kotlinx.android.synthetic.main.fragment_login.view.sentEmailLayout
import kotlinx.android.synthetic.main.fragment_login.view.tick
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {

    private val loginViewModel by viewModel<LoginViewModel>()
    private lateinit var rootView: View
    private val safeArgs: LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_login, container, false)

        val progressDialog = progressDialog(context)
        Utils.setToolbar(activity, getString(R.string.login))
        setHasOptionsMenu(true)
        showSnackbar()

        if (loginViewModel.isLoggedIn())
            popBackStack()

        rootView.loginButton.setOnClickListener {
            loginViewModel.login(email.text.toString(), password.text.toString())
            hideSoftKeyboard(context, rootView)
        }

        loginViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        loginViewModel.showNoInternetDialog
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Utils.showNoInternetDialog(context)
            })

        loginViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(rootView.loginCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        loginViewModel.loggedIn
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loginViewModel.fetchProfile()
            })

        rootView.email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(email: CharSequence, start: Int, before: Int, count: Int) {
                loginViewModel.checkFields(email.toString(), password.text.toString())
            }
        })

        rootView.password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(password: CharSequence, start: Int, before: Int, count: Int) {
                loginViewModel.checkFields(email.text.toString(), password.toString())
            }
        })

        loginViewModel.requestTokenSuccess
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it) {
                    rootView.sentEmailLayout.visibility = View.VISIBLE
                    rootView.loginLayout.visibility = View.GONE
                    Utils.setToolbar(activity, show = false)
                    Utils.navAnimGone(activity?.navigationAuth, requireContext())
                } else {
                    Utils.setToolbar(activity, getString(R.string.login))
                    Utils.navAnimVisible(activity?.navigationAuth, requireContext())
                }
            })

        loginViewModel.isCorrectEmail
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                onEmailEntered(it)
            })

        loginViewModel.areFieldsCorrect
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loginButton.isEnabled = it
            })

        rootView.tick.setOnClickListener {
            rootView.sentEmailLayout.visibility = View.GONE
            Utils.setToolbar(activity, getString(R.string.login))
            Utils.navAnimVisible(activity?.navigationAuth, requireContext())
            rootView.loginLayout.visibility = View.VISIBLE
        }

        rootView.forgotPassword.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            loginViewModel.sendResetPasswordEmail(email.text.toString())
        }

        loginViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                popBackStack()
            })

        return rootView
    }

    private fun popBackStack() {
        findNavController(rootView).popBackStack()
        Snackbar.make(rootView, R.string.welcome_back, Snackbar.LENGTH_SHORT).show()
    }

    private fun onEmailEntered(enable: Boolean) {
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
        safeArgs.snackbarMessage?.let { textSnackbar ->
            Snackbar.make(rootView.loginCoordinatorLayout, textSnackbar, Snackbar.LENGTH_SHORT).show()
        }
    }
}

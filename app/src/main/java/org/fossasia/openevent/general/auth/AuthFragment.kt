package org.fossasia.openevent.general.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.facebook.CallbackManager
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.PLAY_STORE_BUILD_FLAVOR
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.android.synthetic.main.dialog_password.view.textInputLayoutPassword
import kotlinx.android.synthetic.main.dialog_password.view.textInputLayoutConfirmPassword
import kotlinx.android.synthetic.main.dialog_password.view.password
import kotlinx.android.synthetic.main.dialog_password.view.confirmPassword
import kotlinx.android.synthetic.main.fragment_auth.view.*
import org.fossasia.openevent.general.event.EVENT_DETAIL_FRAGMENT
import org.fossasia.openevent.general.notification.NOTIFICATION_FRAGMENT
import org.fossasia.openevent.general.order.ORDERS_FRAGMENT
import org.fossasia.openevent.general.ticket.TICKETS_FRAGMNET
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar

private const val NOT_PROVIDED = "NOT_PROVIDED"

class AuthFragment : Fragment() {
    private lateinit var rootView: View
    private val authViewModel by viewModel<AuthViewModel>()
    private val safeArgs: AuthFragmentArgs by navArgs()
    private val smartAuthViewModel by sharedViewModel<SmartAuthViewModel>()
    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var signUp: SignUp
    private var fbLogIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR) {
            smartAuthViewModel.requestCredentials(SmartAuthUtil.getCredentialsClient(requireActivity()))
            smartAuthViewModel.isCredentialStored
                .nonNull()
                .observe(this, Observer {
                    if (it) redirectToLogin()
                })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_auth, container, false)

        Utils.setToolbar(activity, "", true)
        setHasOptionsMenu(true)

        val progressDialog = progressDialog(context)

        val snackbarMessage = safeArgs.snackbarMessage
        if (!snackbarMessage.isNullOrEmpty()) rootView.snackbar(snackbarMessage)

        rootView.getStartedButton.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            authViewModel.checkUser(rootView.email.text.toString())
        }

        if (getString(R.string.FB_APP_ID) == NOT_PROVIDED) {
            rootView.orTextView.visibility = View.GONE
            rootView.facebookLoginButton.visibility = View.GONE
        }

        rootView.facebookLoginButton.fragment = this
        authViewModel.setFacebookLogin(callbackManager)

        authViewModel.isUserExists
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (fbLogIn) {
                    if (it)
                        redirectToLogin(
                            email = signUp.email.toString(),
                            snackbarMessage = getString(R.string.email_already_registered)
                        )
                    else
                        showPasswordDialog(signUp)
                } else {
                    if (it)
                        redirectToLogin(
                            email = rootView.email.text.toString(),
                            snackbarMessage = getString(R.string.login_to_continue)
                        )
                    else
                        redirectToSignUp()
                }
                authViewModel.mutableStatus.postValue(null)
                authViewModel.mutableFbLogin.postValue(false)
            })

        authViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        smartAuthViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        authViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.rootLayout.longSnackbar(it)
            })

        authViewModel.signedUp
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(R.string.sign_up_success)
                authViewModel.login(signUp)
            })

        authViewModel.signUp
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                this.signUp = it
            })

        authViewModel.fbLogIn
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                this.fbLogIn = it
            })

        authViewModel.loggedIn.observe(viewLifecycleOwner, Observer {
            redirectToMain()
        })

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun redirectToMain() {
        val destinationId =
            when (safeArgs.redirectedFrom) {
                PROFILE_FRAGMENT -> R.id.profileFragment
                EVENT_DETAIL_FRAGMENT -> R.id.eventDetailsFragment
                ORDERS_FRAGMENT -> R.id.orderUnderUserFragment
                TICKETS_FRAGMNET -> R.id.ticketsFragment
                NOTIFICATION_FRAGMENT -> R.id.notificationFragment
                else -> R.id.eventsFragment
            }
        Navigation.findNavController(rootView).popBackStack(destinationId, false)
        rootView.snackbar(R.string.logged_in_automatically)
    }

    private fun showPasswordDialog(signUp: SignUp) {
        val layout = layoutInflater.inflate(R.layout.dialog_password, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.password))
            .setView(layout)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.submit)) { _, _ ->
                Toast.makeText(context, signUp.email, Toast.LENGTH_SHORT).show()
                signUp.password = layout.password.text.toString()
                authViewModel.signUp(signUp)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
                rootView.snackbar(getString(R.string.sign_in_canceled))
            }
            .show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        layout.password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                if (!layout.textInputLayoutPassword.isEndIconVisible) {
                    layout.textInputLayoutPassword.isEndIconVisible = true
                }

                if (layout.password.text.toString().length >= 6) {
                    layout.textInputLayoutPassword.error = null
                    layout.textInputLayoutPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutPassword.error = getString(R.string.invalid_password_message)
                }
                if (layout.confirmPassword.text.toString() == layout.password.text.toString()) {
                    layout.textInputLayoutConfirmPassword.error = null
                    layout.textInputLayoutConfirmPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutConfirmPassword.error =
                        getString(R.string.invalid_confirm_password_message)
                }
                when (layout.textInputLayoutConfirmPassword.isErrorEnabled ||
                    layout.textInputLayoutPassword.isErrorEnabled) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })

        layout.confirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                if (!layout.textInputLayoutConfirmPassword.isEndIconVisible) {
                    layout.textInputLayoutConfirmPassword.isEndIconVisible = true
                }

                if (layout.confirmPassword.text.toString() == layout.password.text.toString()) {
                    layout.textInputLayoutConfirmPassword.error = null
                    layout.textInputLayoutConfirmPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutConfirmPassword.error =
                        getString(R.string.invalid_confirm_password_message)
                }
                when (layout.textInputLayoutConfirmPassword.isErrorEnabled ||
                    layout.textInputLayoutPassword.isErrorEnabled ||
                    layout.password.text.toString().length < 6) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })
    }

    private fun redirectToLogin(email: String = "", snackbarMessage: String = "") {
        Navigation.findNavController(rootView)
            .navigate(AuthFragmentDirections
                .actionAuthToLogIn(email = email,
                    redirectedFrom = safeArgs.redirectedFrom,
                    snackbarMessage = snackbarMessage
                )
            )
    }

    private fun redirectToSignUp() {
        Navigation.findNavController(rootView)
            .navigate(AuthFragmentDirections
                .actionAuthToSignUp(rootView.email.text.toString(), safeArgs.redirectedFrom)
            )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

package org.fossasia.openevent.general.auth

import android.support.v4.app.Fragment
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.credentials.Credential
import org.fossasia.openevent.general.utils.nullToEmpty

const val SAVE_DATA: Int = 1
const val FETCH_DATA: Int = 3

class LoginFragment : Fragment() {

    private val loginActivityViewModel by viewModel<LoginFragmentViewModel>()
    private lateinit var rootView: View
    private var googleApiClient: GoogleApiClient? = null
    private lateinit var googleAuthBuilder: GoogleAuthBuilder
    private lateinit var credentialLogin: Credential

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_login, container, false)

        val context = context
        val activity = activity
        if(context != null && activity!= null) {
            googleAuthBuilder = GoogleAuthBuilder(context, activity)
            googleApiClient = googleAuthBuilder.googleApiClient
        }

        if (loginActivityViewModel.isLoggedIn())
            googleAuthBuilder.redirectToMain()

        //Initialize credentialLogin
        credentialLogin = Credential.Builder("id")
                .setPassword("password")
                .build()

        rootView.loginButton.setOnClickListener {
            googleAuthBuilder.mode = SAVE_DATA
            if (username.text.isNotEmpty() && password.text.isNotEmpty()) {
                credentialLogin = Credential.Builder(username.text.toString())
                        .setPassword(password.text.toString())
                        .build()
            }
            loginActivityViewModel.login(username.text.toString(), password.text.toString())
        }

        loginActivityViewModel.progress.observe(this, Observer {
            it?.let {
                Utils.showProgressBar(rootView.progressBar, it)
                loginButton.isEnabled = !it
            }
        })

        loginActivityViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        loginActivityViewModel.loggedIn.observe(this, Observer {
            Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show()
            if (googleAuthBuilder.mode == FETCH_DATA) {
                googleAuthBuilder.redirectToMain()
            } else if (googleAuthBuilder.mode == SAVE_DATA) {
                googleAuthBuilder.saveCredential(credentialLogin)
            }
        })

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FETCH_DATA) {
            googleAuthBuilder.mode = FETCH_DATA
            val credential = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
            credential?.let {
                processRetrievedCredential(credential)
            }
        } else if (requestCode == SAVE_DATA) {
            googleAuthBuilder.mode = SAVE_DATA
            googleAuthBuilder.redirectToMain()
        }
    }

    fun processRetrievedCredential(credential: Credential) {
        loginActivityViewModel.login(credential.id.nullToEmpty(), credential.password.nullToEmpty())
        username.setText(credential.id)
        password.setText(credential.password)
    }

    override fun onPause() {
        activity?.let { googleApiClient?.stopAutoManage(it) }
        googleApiClient?.disconnect()
        super.onPause()
    }

    override fun onResume() {
        if (googleApiClient?.isConnected == false) {
            googleApiClient?.connect()
        }
        super.onResume()
    }
}
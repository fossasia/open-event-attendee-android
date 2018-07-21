package org.fossasia.openevent.general.auth

import android.support.v4.app.Fragment
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.api.Status
import org.fossasia.openevent.general.utils.nullToEmpty
import timber.log.Timber

const val SAVE_DATA: Int = 1
const val FETCH_DATA: Int = 3

class LoginFragment : Fragment() {

    private val loginActivityViewModel by viewModel<LoginFragmentViewModel>()
    private lateinit var rootView: View
    var googleApiClient: GoogleApiClient? = null
    private lateinit var googleAuthBuilder: GoogleAuthBuilder
    private lateinit var credentialLogin: Credential
    private var isResolving: Boolean = false

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
                saveCredential(credentialLogin)
            }
        })

        return rootView
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

    fun resolveResult(status: Status, requestCode: Int) {
        if (isResolving) {
            return
        }

        if (status.hasResolution()) {
            try {
                startIntentSenderForResult(status.resolution.intentSender, requestCode, null, 0, 0, 0, null)
                isResolving = true
            } catch (e: IntentSender.SendIntentException) {
                Timber.e(e)
            }
        } else {
            Timber.e("Resolution Failed!")
        }
    }

    fun saveCredential(credential: Credential) {
        isResolving = false
        Auth.CredentialsApi.save(googleApiClient, credential).setResultCallback { status ->

            if (status.isSuccess) {
                Timber.d("Credential saved")
                googleAuthBuilder.redirectToMain()
            } else {
                Timber.d("Attempt to save credential failed ${status.statusMessage} ${status.statusCode}")
                resolveResult(status, SAVE_DATA)
            }
        }
    }

    override fun onResume() {
        if (googleApiClient?.isConnected == false) {
            googleApiClient?.connect()
        }
        super.onResume()
    }
}
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
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.Status
import android.content.IntentSender
import org.fossasia.openevent.general.MainActivity
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.auth.api.credentials.CredentialRequest
import org.fossasia.openevent.general.utils.nullToEmpty
import timber.log.Timber

class LoginFragment : Fragment(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val loginActivityViewModel by viewModel<LoginFragmentViewModel>()
    private lateinit var rootView: View
    private var googleApiClient: GoogleApiClient? = null
    private var isResolving: Boolean = false
    private var isRequesting: Boolean = false
    private var SAVE_DATA: Int = 1
    private var FETCH_DATA: Int = 3
    private lateinit var credentialLogin: Credential
    private var mode: Int = 0

    override fun onConnected(p0: Bundle?) {
        //Request credentials from a logged in account
        Auth.CredentialsApi.disableAutoSignIn(googleApiClient)
        requestCredentials()
    }

    override fun onConnectionSuspended(p0: Int) {}

    override fun onConnectionFailed(p0: ConnectionResult) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_login, container, false)

        val context = context
        val activity = activity
        if(context != null && activity!= null) {
            googleApiClient =  GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .enableAutoManage(activity, 0, this)
                    .addApi(Auth.CREDENTIALS_API)
                    .build()
        }

        if (loginActivityViewModel.isLoggedIn())
            redirectToMain()

        //Initialize credentialLogin
        credentialLogin = Credential.Builder("id")
                .setPassword("password")
                .build()

        rootView.loginButton.setOnClickListener {
            mode = SAVE_DATA
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
            if (mode == FETCH_DATA) {
                redirectToMain()
            } else if (mode == SAVE_DATA) {
                saveCredential(credentialLogin)
            }
        })

        return rootView
    }

    private fun redirectToMain() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        activity?.finish()
    }

    private fun saveCredential(credential: Credential) {
        isResolving = false
        Auth.CredentialsApi.save(googleApiClient, credential).setResultCallback { status ->

            if (status.isSuccess) {
                Timber.d("Credential saved")
                redirectToMain()
            } else {
                Timber.d("Attempt to save credential failed ${status.statusMessage} ${status.statusCode}")
                resolveResult(status, SAVE_DATA)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FETCH_DATA) {
            mode = FETCH_DATA
            val credential = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
            credential?.let { processRetrievedCredential(it) }
        } else if (requestCode == SAVE_DATA) {
            mode = SAVE_DATA
            redirectToMain()
        }
    }

    private fun resolveResult(status: Status, requestCode: Int) {
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

    private fun requestCredentials() {
        isRequesting = true

        val request = CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build()

        Auth.CredentialsApi.request(googleApiClient, request).setResultCallback { credentialRequestResult ->
            isRequesting = false
            val status = credentialRequestResult.status
            if (status.isSuccess) {
                val credential = credentialRequestResult.credential
                processRetrievedCredential(credential)
            } else if (status.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                resolveResult(status, FETCH_DATA)
            }
        }
    }

    private fun processRetrievedCredential(credential: Credential) {
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
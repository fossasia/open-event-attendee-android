package org.fossasia.openevent.general.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat.startActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R

class GoogleAuthBuilder(private val context: Context, private val activity: FragmentActivity): GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    var googleApiClient: GoogleApiClient? = null
    var mode: Int = 0

    override fun onConnected(p0: Bundle?) {
        Auth.CredentialsApi.disableAutoSignIn(googleApiClient)
        requestCredentials()
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init {
        googleApiClient = GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .enableAutoManage(activity, 0, this)
                .addApi(Auth.CREDENTIALS_API)
                .build()
    }

    fun requestCredentials() {
        val request = CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build()

        Auth.CredentialsApi.request(googleApiClient, request).setResultCallback { credentialRequestResult ->
            val status = credentialRequestResult.status
            if (status.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                resolveResult(status, FETCH_DATA)
            }
        }
    }

    fun redirectToMain() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(context, intent, null)
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        activity.finish()
    }

}
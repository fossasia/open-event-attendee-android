package org.fossasia.openevent.general.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.app.ActivityCompat.startIntentSenderForResult
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat.startActivity
import android.widget.EditText
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.nullToEmpty
import timber.log.Timber

class GoogleAuthBuilder(private val context: Context, private val activity: FragmentActivity): GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    var googleApiClient: GoogleApiClient? = null
    private var isResolving: Boolean = false
    var mode: Int = 0
    var username: EditText? = null
    var password: EditText? = null
    var loginActivityViewModel: LoginFragmentViewModel? = null

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

    @SuppressLint("RestrictedApi")
    fun resolveResult(status: Status, requestCode: Int) {
        if (isResolving) {
            return
        }

        if (status.hasResolution()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startIntentSenderForResult(activity, status.resolution.intentSender, requestCode, null, 0, 0, 0, null)
                }
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
                redirectToMain()
            } else {
                Timber.d("Attempt to save credential failed ${status.statusMessage} ${status.statusCode}")
                resolveResult(status, SAVE_DATA)
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
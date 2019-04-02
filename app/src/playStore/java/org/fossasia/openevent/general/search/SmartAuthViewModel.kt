package org.fossasia.openevent.general.search

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsClient
import com.google.android.gms.auth.api.credentials.IdentityProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.OnCompleteListener
import timber.log.Timber

const val RC_CREDENTIALS_READ = 2
const val RC_CREDENTIALS_SAVE = 3

class SmartAuthViewModel : ViewModel() {
    private var requestedCredentialsEarlier = false
    private lateinit var credentialsClient: CredentialsClient
    private lateinit var signInClient: GoogleSignInClient
    private lateinit var thisActivity: Activity
    private val mutableId = MutableLiveData<String>()
    val id: LiveData<String> = mutableId
    private val mutablePassword = MutableLiveData<String>()
    val password: LiveData<String> = mutablePassword
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress

    fun buildCredential (activity: Activity?, accountName: String?) {
        if (activity == null) return
        thisActivity = activity
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        if (accountName != null) {
            gsoBuilder.setAccountName(accountName)
        }
        credentialsClient = Credentials.getClient(activity)
        signInClient = GoogleSignIn.getClient(activity, gsoBuilder.build())
    }

    fun requestCredentials(activity: Activity?) {
        if (activity == null || requestedCredentialsEarlier) return
        val crBuilder = CredentialRequest.Builder()
            .setPasswordLoginSupported(true)
        crBuilder.setAccountTypes(IdentityProviders.GOOGLE)
        mutableProgress.value = true
        credentialsClient.request(crBuilder.build()).addOnCompleteListener(
            OnCompleteListener<CredentialRequestResponse> { task ->
                requestedCredentialsEarlier = true

                mutableProgress.value = false
                if (task.isSuccessful) {
                    mutableId.value = task.result?.credential?.id
                    mutablePassword.value = task.result?.credential?.password
                    return@OnCompleteListener
                }
                val e = task.exception
                if (e is ResolvableApiException) {
                    resolveResult(e, RC_CREDENTIALS_READ, activity)
                } else {
                    Timber.e("request: not handling exception:$e")
                }
            })
    }

    fun saveCredential (activity: Activity?, id: String, password: String) {
        if (activity == null) return
        val credential = Credential.Builder(id).setPassword(password).build()
        credentialsClient.save(credential).addOnCompleteListener(
            OnCompleteListener<Void> { task ->
                if (task.isSuccessful)
                    return@OnCompleteListener

                val e = task.exception
                if (e is ResolvableApiException) {
                    resolveResult(e, RC_CREDENTIALS_SAVE, activity)
                } else {
                    Timber.e("save:FAILURE$e")
                }
            })
    }

    private fun resolveResult(rae: ResolvableApiException?, requestCode: Int, activity: Activity) {
        rae?.startResolutionForResult(activity, requestCode)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity) {
        Timber.e("$requestCode $resultCode")
        if (requestCode == RC_CREDENTIALS_READ) {
            if (resultCode == Activity.RESULT_OK) {
                val credential = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                buildCredential(activity, credential?.id)
            }
        }
    }
}

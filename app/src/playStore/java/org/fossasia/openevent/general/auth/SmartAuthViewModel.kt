package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse
import com.google.android.gms.auth.api.credentials.CredentialsClient
import com.google.android.gms.auth.api.credentials.IdentityProviders
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.OnCompleteListener
import timber.log.Timber

const val RC_CREDENTIALS_READ = 2
const val RC_CREDENTIALS_SAVE = 3

class SmartAuthViewModel : ViewModel() {
    private var requestedCredentialsEarlier = false
    val mutableId = MutableLiveData<String>()
    val id: LiveData<String> = mutableId
    private val mutablePassword = MutableLiveData<String>()
    val password: LiveData<String> = mutablePassword
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableApiExceptionRequestCodePair = MutableLiveData<Pair<ResolvableApiException, Int>>()
    val apiExceptionCodePair: LiveData<Pair<ResolvableApiException, Int>> = mutableApiExceptionRequestCodePair
    private val mutableStatus = MutableLiveData<Boolean>()
    val isCredentialStored: LiveData<Boolean> = mutableStatus

    fun requestCredentials(credentialsClient: CredentialsClient) {
        if (requestedCredentialsEarlier) return

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
                    mutableStatus.value = true
                    return@OnCompleteListener
                } else {
                    mutableStatus.value = false
                }
                val e = task.exception
                if (e is ResolvableApiException) {
                    mutableApiExceptionRequestCodePair.value = Pair(e, RC_CREDENTIALS_READ)
                } else {
                    Timber.e("request: not handling exception: $e")
                }
            })
    }

    fun saveCredential(id: String, password: String, credentialsClient: CredentialsClient) {
        val credential = Credential.Builder(id).setPassword(password).build()
        credentialsClient.save(credential).addOnCompleteListener(
            OnCompleteListener<Void> { task ->

                if (task.isSuccessful)
                    return@OnCompleteListener
                val e = task.exception
                if (e is ResolvableApiException) {
                    mutableApiExceptionRequestCodePair.value = Pair(e, RC_CREDENTIALS_SAVE)
                } else {
                    Timber.e("save:FAILURE $e")
                }
            })
    }
}

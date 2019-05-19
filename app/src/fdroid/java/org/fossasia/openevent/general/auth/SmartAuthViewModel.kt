package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

const val RC_CREDENTIALS_READ = 2
class SmartAuthViewModel : ViewModel() {

    val mutableId = MutableLiveData<String>()
    val id: LiveData<String> = mutableId
    private val mutablePassword = MutableLiveData<String>()
    val password: LiveData<String> = mutablePassword
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableApiExceptionRequestCodePair = MutableLiveData<Pair<Any, Any>>()
    val apiExceptionCodePair: LiveData<Pair<Any, Any>> = mutableApiExceptionRequestCodePair
    private val mutableStatus = MutableLiveData<Boolean>()
    val isCredentialStored: LiveData<Boolean> = mutableStatus

    fun requestCredentials(any: Any) {
        return
    }

    fun saveCredential(any1: Any, any2: Any, any3: Any) {
        return
    }
}

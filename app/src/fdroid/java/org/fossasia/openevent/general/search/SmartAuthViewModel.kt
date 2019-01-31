package org.fossasia.openevent.general.search

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

const val RC_CREDENTIALS_READ = 2
const val RC_CREDENTIALS_SAVE = 3

class SmartAuthViewModel : ViewModel() {
    private val mutableId = MutableLiveData<String>()
    val id: LiveData<String> = mutableId
    private val mutablePassword = MutableLiveData<String>()
    val password: LiveData<String> = mutablePassword
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress

    fun buildCredential (activity: Activity?, accountName: String?) {
        return
    }

    fun requestCredentials(activity: Activity?) {
        return
    }

    fun saveCredential (activity: Activity?, id: String, password: String) {
        return
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity) {
        return
    }
}

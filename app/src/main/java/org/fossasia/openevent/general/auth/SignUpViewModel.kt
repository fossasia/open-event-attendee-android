package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.utils.nullToEmpty
import timber.log.Timber

class SignUpViewModel(
    private val authService: AuthService,
    private val network: Network,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableShowNoInternetDialog = MutableLiveData<Boolean>()
    val showNoInternetDialog: LiveData<Boolean> = mutableShowNoInternetDialog
    private val mutableLoggedIn = SingleLiveEvent<Boolean>()
    var loggedIn: LiveData<Boolean> = mutableLoggedIn

    var email: String? = null
    var password: String? = null

    fun signUp(signUp: SignUp) {
        if (!isConnected()) return

        compositeDisposable += authService.signUp(signUp)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                login(signUp)
                Timber.d("Success!")
            }, {
                when {
                    it.toString().contains("HTTP 409") ->
                        mutableError.value = resource.getString(R.string.sign_up_fail_email_exist_message)
                    it.toString().contains("HTTP 422") ->
                        mutableError.value = resource.getString(R.string.sign_up_fail_email_invalid_message)
                    else -> mutableError.value = resource.getString(R.string.sign_up_fail_message)
                }
                Timber.d(it, "Failed")
            })
    }

    private fun login(signUp: SignUp) {
        if (!isConnected()) return
        email = signUp.email
        password = signUp.password
        compositeDisposable += authService.login(email.nullToEmpty(), password.nullToEmpty())
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableLoggedIn.value = true
                Timber.d("Success!")
                fetchProfile()
            }, {
                mutableError.value = resource.getString(R.string.login_automatically_fail_message)
                Timber.d(it, "Failed")
            })
    }

    private fun fetchProfile() {
        if (!isConnected()) return
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Fetched User Details")
            }) {
                Timber.e(it, "Error loading user details")
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        if (!isConnected) mutableShowNoInternetDialog.value = true
        return isConnected
    }
}

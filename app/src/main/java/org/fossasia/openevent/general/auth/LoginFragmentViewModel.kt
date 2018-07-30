package org.fossasia.openevent.general.auth

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Patterns
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Network
import timber.log.Timber

class LoginFragmentViewModel(private val authService: AuthService,
                             private val network: Network) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val user = MutableLiveData<User>()
    val error = SingleLiveEvent<String>()
    val showNoInternetDialog = MutableLiveData<Boolean>()
    val requestTokenSuccess = MutableLiveData<Boolean>()
    val loggedIn = SingleLiveEvent<Boolean>()

    fun isLoggedIn() = authService.isLoggedIn()

    fun login(email: String, password: String) {
        isConnected()
        if (hasErrors(email, password)) return
        compositeDisposable.add(authService.login(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    loggedIn.value = true
                }, {
                    error.value = "Unable to Login. Please check your credentials"
                }))
    }

    private fun hasErrors(email: String?, password: String?): Boolean {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            error.value = "Email or Password cannot be empty!"
            return true
        }
        return false
    }

    fun showForgotPassword(email: String): Boolean {
        if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true
        }
        return false
    }

    fun sendResetPasswordEmail(email: String) {
        isConnected()
        compositeDisposable.add(authService.sendResetPasswordEmail(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    requestTokenSuccess.value = verifyMessage(it.message)
                }, {
                    error.value = "Email address not present in server. Please check your email"
                }))
    }

    fun verifyMessage(message: String): Boolean {
        if (message.equals("Email Sent")) {
            return true
        }
        return false
    }

    fun fetchProfile() {
        isConnected()
        compositeDisposable.add(authService.getProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progress.value = true
                }).doFinally {
                    progress.value = false
                }.subscribe({ it ->
                    Timber.d("User Fetched")
                    user.value = it
                }) {
                    Timber.e(it, "Failure")
                    error.value = "Failure"
        })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun showNoInternetDialog() {
        showNoInternetDialog.value = network.isNetworkConnected()
    }

    fun isConnected() {
        if (!network.isNetworkConnected()) {
            showNoInternetDialog.value = true
            return
        }
    }
}
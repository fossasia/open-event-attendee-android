package org.fossasia.openevent.general.auth

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.utils.nullToEmpty
import timber.log.Timber

class SignUpFragmentViewModel(private val authService: AuthService, private val resource: Resource) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val error = SingleLiveEvent<String>()
    val signedUp = MutableLiveData<User>()
    val loggedIn = SingleLiveEvent<Boolean>()
    var email: String? = null
    var password: String? = null

    fun signUp(signUp: SignUp, confirmPassword: String) {
        email = signUp.email
        password = signUp.password

        if (hasErrors(email, password, confirmPassword)) return
        compositeDisposable.add(authService.signUp(signUp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    signedUp.value = it
                    Timber.d("Success!")
                }, {
                    error.value = "Unable to SignUp!"
                    Timber.d(it, "Failed")
                }))
    }

    fun login(signUp: SignUp) {
        email = signUp.email
        password = signUp.password
        compositeDisposable.add(authService.login(email.nullToEmpty(), password.nullToEmpty())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    progress.value = true
                }.doFinally {
                    progress.value = false
                }.subscribe({
                    loggedIn.value = true
                    Timber.d("Success!")
                    fetchProfile()
                }, {
                    error.value = "Unable to Login automatically"
                    Timber.d(it, "Failed")
                }))
    }

    fun fetchProfile() {
        compositeDisposable.add(authService.getProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ user ->
                    Timber.d("Fetched User Details")
                }) {
                    Timber.e(it, "Error loading user details")
                })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun hasErrors(email: String?, password: String?, confirmPassword: String): Boolean {
        if (!isNetworkConnected()) {
            return true
        }
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            error.value = "Email or Password cannot be empty!"
            return true
        }
        if (password != confirmPassword) {
            error.value = "Passwords do not match!"
            return true
        }
        return false
    }

    fun isNetworkConnected(): Boolean {
        return resource.isNetworkConnected()
    }
}
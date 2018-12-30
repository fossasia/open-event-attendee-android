package org.fossasia.openevent.general.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.utils.nullToEmpty
import timber.log.Timber

class SignUpViewModel(
    private val authService: AuthService,
    private val network: Network
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableError = MutableLiveData<String>()
    val error: LiveData<String> = mutableError
    private val mutableSignedUp = MutableLiveData<User>()
    val signedUp: LiveData<User> = mutableSignedUp
    private val mutableShowNoInternetDialog = MutableLiveData<Boolean>()
    val showNoInternetDialog: LiveData<Boolean> = mutableShowNoInternetDialog
    private val mutableLoggedIn = SingleLiveEvent<Boolean>()
    var loggedIn: LiveData<Boolean> = mutableLoggedIn

    var email: String? = null
    var password: String? = null

    fun signUp(signUp: SignUp, confirmPassword: String) {
        if (!isConnected()) return
        email = signUp.email
        password = signUp.password

        if (hasErrors(email, password, confirmPassword)) return
        compositeDisposable.add(authService.signUp(signUp)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableSignedUp.value = it
                Timber.d("Success!")
            }, {
                when {
                    it.toString().contains("HTTP 409 CONFLICT") ->
                        mutableError.value = "Unable to SignUp: Email already exists!"
                    it.toString().contains("HTTP 422 UNPROCESSABLE ENTITY") ->
                        mutableError.value = "Unable to SignUp: Not a valid email address!"
                    else -> mutableError.value = "Unable to SignUp!"
                }
                Timber.d(it, "Failed")
            })
        )
    }

    fun login(signUp: SignUp) {
        if (!isConnected()) return
        email = signUp.email
        password = signUp.password
        compositeDisposable.add(authService.login(email.nullToEmpty(), password.nullToEmpty())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableLoggedIn.value = true
                Timber.d("Success!")
                fetchProfile()
            }, {
                mutableError.value = "Unable to Login automatically"
                Timber.d(it, "Failed")
            })
        )
    }

    private fun fetchProfile() {
        if (!isConnected()) return
        compositeDisposable.add(authService.getProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
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
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            mutableError.value = "Email or Password cannot be empty!"
            return true
        }
        if (password != confirmPassword) {
            mutableError.value = "Passwords do not match!"
            return true
        }

        if (password.length < 6) {
            mutableError.value = "Password should be atleast 6 characters!"
            return true
        }

        return false
    }

    private fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        if (!isConnected) mutableShowNoInternetDialog.value = true
        return isConnected
    }
}

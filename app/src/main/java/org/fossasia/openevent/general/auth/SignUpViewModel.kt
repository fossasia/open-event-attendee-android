package org.fossasia.openevent.general.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
    private val mutableSignedUp = MutableLiveData<User>()
    val signedUp: LiveData<User> = mutableSignedUp
    private val mutableShowNoInternetDialog = MutableLiveData<Boolean>()
    val showNoInternetDialog: LiveData<Boolean> = mutableShowNoInternetDialog
    private val mutableLoggedIn = SingleLiveEvent<Boolean>()
    var loggedIn: LiveData<Boolean> = mutableLoggedIn
    private val mutableAreFieldsCorrect = MutableLiveData<Boolean>(false)
    val areFieldsCorrect: LiveData<Boolean> = mutableAreFieldsCorrect

    var email: String? = null
    var password: String? = null

    fun signUp(signUp: SignUp, confirmPassword: String) {
        if (!isConnected()) return
        email = signUp.email
        password = signUp.password

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
                    it.toString().contains("HTTP 409") ->
                        mutableError.value = resource.getString(R.string.sign_up_fail_email_exist_message)
                    it.toString().contains("HTTP 422") ->
                        mutableError.value = resource.getString(R.string.sign_up_fail_email_invalid_message)
                    else -> mutableError.value = resource.getString(R.string.sign_up_fail_message)
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
                mutableError.value = resource.getString(R.string.login_automatically_fail_message)
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

    fun checkFields(email: String, password: String, confirmPassword: String) {
        mutableAreFieldsCorrect.value = email.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            password.isNotEmpty() &&
            password.length > 5 &&
            confirmPassword == password
    }

    private fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        if (!isConnected) mutableShowNoInternetDialog.value = true
        return isConnected
    }
}

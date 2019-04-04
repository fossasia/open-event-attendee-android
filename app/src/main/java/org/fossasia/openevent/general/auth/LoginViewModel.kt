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
import timber.log.Timber

class LoginViewModel(
    private val authService: AuthService,
    private val network: Network,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError
    private val mutableShowNoInternetDialog = MutableLiveData<Boolean>()
    val showNoInternetDialog: LiveData<Boolean> = mutableShowNoInternetDialog
    private val mutableRequestTokenSuccess = MutableLiveData<Boolean>()
    val requestTokenSuccess: LiveData<Boolean> = mutableRequestTokenSuccess
    private val mutableIsCorrectEmail = MutableLiveData<Boolean>(false)
    val isCorrectEmail: LiveData<Boolean> = mutableIsCorrectEmail
    private val mutableLoggedIn = SingleLiveEvent<Boolean>()
    var loggedIn: LiveData<Boolean> = mutableLoggedIn
    private val mutableAreFieldsCorrect = MutableLiveData<Boolean>(false)
    val areFieldsCorrect: LiveData<Boolean> = mutableAreFieldsCorrect

    fun isLoggedIn() = authService.isLoggedIn()

    fun login(email: String, password: String) {
        if (!isConnected()) return
        compositeDisposable.add(authService.login(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableLoggedIn.value = true
            }, {
                mutableError.value = resource.getString(R.string.login_fail_message)
            })
        )
    }

    fun sendResetPasswordEmail(email: String) {
        if (!isConnected()) return
        compositeDisposable.add(authService.sendResetPasswordEmail(email)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableRequestTokenSuccess.value = verifyMessage(it.message)
            }, {
                mutableRequestTokenSuccess.value = verifyMessage(it.message.toString())
                mutableError.value = resource.getString(R.string.email_not_in_server_message)
            })
        )
    }

    private fun verifyMessage(message: String): Boolean {
        if (message == resource.getString(R.string.email_sent)) {
            return true
        }
        return false
    }

    fun fetchProfile() {
        if (!isConnected()) return
        compositeDisposable.add(authService.getProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                Timber.d("User Fetched")
                mutableUser.value = it
            }) {
                Timber.e(it, "Failure")
                mutableError.value = resource.getString(R.string.failure)
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun checkFields(email: String, password: String) {
        val isEmailCorrect = email.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        mutableIsCorrectEmail.value = isEmailCorrect
        mutableAreFieldsCorrect.value = isEmailCorrect &&
            password.isNotEmpty()
    }

    private fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        if (!isConnected) mutableShowNoInternetDialog.value = true
        return isConnected
    }
}

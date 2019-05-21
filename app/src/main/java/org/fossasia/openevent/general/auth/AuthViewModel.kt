package org.fossasia.openevent.general.auth

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.GraphRequest
import com.facebook.CallbackManager
import org.fossasia.openevent.general.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.data.Network
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.utils.nullToEmpty

private const val EMAIL = "email"
private const val FIRST_NAME = "first_name"
private const val LAST_NAME = "last_name"

class AuthViewModel(
    private val authService: AuthService,
    private val network: Network,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    val mutableStatus = MutableLiveData<Boolean>()
    val isUserExists: LiveData<Boolean> = mutableStatus
    private val mutableError = MutableLiveData<String>()
    val error: LiveData<String> = mutableError
    private val mutableSignedUp = MutableLiveData<User>()
    val signedUp: LiveData<User> = mutableSignedUp
    private val mutableLoggedIn = SingleLiveEvent<Boolean>()
    var loggedIn: LiveData<Boolean> = mutableLoggedIn
    val mutableFbLogin = MutableLiveData<Boolean>()
    val fbLogIn: LiveData<Boolean> = mutableFbLogin
    val mutableSignUp = MutableLiveData<SignUp>()
    val signUp: LiveData<SignUp> = mutableSignUp

    fun checkUser(email: String) {
        if (!network.isNetworkConnected()) {
            mutableError.value = resource.getString(R.string.no_internet_message)
            return
        }
        compositeDisposable += authService.checkEmail(email)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableStatus.value = !it.result
                Timber.d("Success!")
            }, {
                mutableError.value = resource.getString(R.string.error)
                Timber.d(it, "Failed")
            })
    }

    fun signUp(signUp: SignUp) {
        if (!network.isNetworkConnected()) {
            mutableError.value = resource.getString(R.string.no_internet_message)
            return
        }

        compositeDisposable += authService.signUp(signUp)
            .withDefaultSchedulers()
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
    }

    fun login(signUp: SignUp) {
        if (!network.isNetworkConnected()) {
            mutableError.value = resource.getString(R.string.no_internet_message)
            return
        }
        compositeDisposable += authService.login(signUp.email.nullToEmpty(), signUp.password.nullToEmpty())
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableLoggedIn.value = true
                Timber.d("Success!")
            }, {
                mutableError.value = resource.getString(R.string.login_automatically_fail_message)
                Timber.d(it, "Failed")
            })
    }

    fun setFacebookLogin(callbackManager: CallbackManager) {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    LoginManager.getInstance().logOut()
                    fetchUserProfile(loginResult.accessToken)
                }

                override fun onCancel() {
                    mutableError.value = "Cancelled"
                }

                override fun onError(exception: FacebookException) {
                    mutableError.value = "Error"
                    Timber.e(exception)
                }
            })
    }

    private fun fetchUserProfile(token: AccessToken?) {
        val request = GraphRequest.newMeRequest(
            token
        ) { data, _ ->
            if (!data.has(EMAIL)) {
                mutableError.value = resource.getString(R.string.fb_email_not_registered)
            } else {
                mutableSignUp.value = SignUp(
                    firstName = data.getString(FIRST_NAME),
                    lastName = data.getString(LAST_NAME),
                    email = data.getString(EMAIL)
                )
                mutableFbLogin.value = true
                checkUser(data.getString(EMAIL))
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", "$FIRST_NAME,$LAST_NAME,$EMAIL")
        request.parameters = parameters
        request.executeAsync()
    }
}

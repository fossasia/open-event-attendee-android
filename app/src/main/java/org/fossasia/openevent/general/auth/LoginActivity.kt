package org.fossasia.openevent.general.auth

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.android.ext.android.inject
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val authService: AuthService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authService.isLoggedIn())
            redirectToMain()

        setContentView(R.layout.activity_login)

        loginButton.setOnClickListener { _ ->
            loginUser(username.text.toString(), password.text.toString())
        }
    }

    private fun redirectToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loginUser(email: String, password: String) {
        compositeDisposable.add(authService.login(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    showProgress(true)
                }
                .doFinally {
                    showProgress(false)
                }
                .subscribe({ _ ->
                    Toast.makeText(applicationContext, "Success!", Toast.LENGTH_LONG).show()
                    redirectToMain()
                }) { throwable ->
                    Toast.makeText(applicationContext, "Unable to Login!", Toast.LENGTH_LONG).show()
                    Timber.e(throwable, "Failure in logging in")
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun showProgress(enabled: Boolean) {
        loginButton.isEnabled = !enabled
        progressBar.visibility = if (enabled) View.VISIBLE else View.GONE
    }
}
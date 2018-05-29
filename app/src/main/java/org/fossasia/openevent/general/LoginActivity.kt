package org.fossasia.openevent.general

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.fossasia.openevent.general.model.Login
import org.fossasia.openevent.general.rest.ApiClient
import org.fossasia.openevent.general.utils.ConstantStrings
import org.fossasia.openevent.general.utils.SharedPreferencesUtil
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = SharedPreferencesUtil.getString(ConstantStrings.TOKEN, null)
        Timber.d("Token is %s", token)
        ApiClient.setToken("JWT $token")
        if (token != null)
            redirectToMain()

        setContentView(R.layout.activity_login)

        progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog.setMessage("Logging you in...")

        login.setOnClickListener { _ ->
            progressDialog.show()
            loginUser(username.text.toString(), password.text.toString())
        }
    }

    private fun redirectToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loginUser(email: String, password: String) {
        val login = Login(email.trim(), password.trim())
        compositeDisposable.add(ApiClient.eventApi.login(login)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (_, accessToken) ->
                    Toast.makeText(applicationContext, "Success!", Toast.LENGTH_LONG).show()
                    ApiClient.setToken(accessToken)
                    progressDialog.cancel()
                    SharedPreferencesUtil.putString(ConstantStrings.TOKEN, accessToken)
                    if (accessToken != null)
                        redirectToMain()
                }) { throwable ->
                    ApiClient.setToken(null)
                    progressDialog.cancel()
                    Toast.makeText(applicationContext, "Unable to Login!", Toast.LENGTH_LONG).show()
                    Timber.e(throwable, "Failure in logging in")
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
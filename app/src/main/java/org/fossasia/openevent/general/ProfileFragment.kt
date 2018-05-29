package org.fossasia.openevent.general

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.fossasia.openevent.general.rest.ApiClient
import org.fossasia.openevent.general.utils.ConstantStrings
import org.fossasia.openevent.general.utils.JWTUtils
import org.fossasia.openevent.general.utils.SharedPreferencesUtil
import org.json.JSONException
import timber.log.Timber


class ProfileFragment : Fragment() {
    private var userId: Long = -1
    private val compositeDisposable = CompositeDisposable()
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var token: String? = SharedPreferencesUtil.getString(ConstantStrings.TOKEN, null)
        if (token == null) {
            redirectToLogin()
            return
        }
        token = "JWT $token"
        try {
            userId = JWTUtils.getIdentity(token).toLong()
            Timber.d("User id is %s", userId)
            ApiClient.setToken(token)
        } catch (e: JSONException) {
            Timber.e(e, "Unable to parse JWT %s", token)
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(activity, LoginActivity::class.java))
    }

    private fun redirectToMain() {
        startActivity(Intent(activity, MainActivity::class.java))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        rootView.logout.setOnClickListener { _ ->
            SharedPreferencesUtil.remove(ConstantStrings.TOKEN)
            ApiClient.setToken(null)
            redirectToMain()
        }

        rootView.progressBar.isIndeterminate = true

        compositeDisposable.add(ApiClient.eventApi.getProfile(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ user ->
                    rootView.progressBar.isIndeterminate = false
                    rootView.progressBar.visibility = View.GONE
                    Timber.d("Response Success")
                    rootView.name.text = "${user.firstName} ${user.lastName}"
                    rootView.email.text = user.email

                    Picasso.get()
                            .load(user.avatarUrl)
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .transform(CircleTransform())
                            .into(rootView.avatar)
                }) { throwable -> Timber.e(throwable, "Failure") })
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}
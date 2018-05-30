package org.fossasia.openevent.general.auth

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
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.android.ext.android.inject
import timber.log.Timber


class ProfileFragment : Fragment() {
    private val compositeDisposable = CompositeDisposable()
    private val authService: AuthService by inject()
    private lateinit var rootView: View

    private fun redirectToLogin() {
        startActivity(Intent(activity, LoginActivity::class.java))
    }

    private fun redirectToMain() {
        startActivity(Intent(activity, MainActivity::class.java))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        if (!authService.isLoggedIn())
            redirectToLogin()

        rootView.logout.setOnClickListener {
            authService.logout()
            redirectToMain()
        }

        fetchProfile()

        return rootView
    }



    private fun fetchProfile() {
        if (!authService.isLoggedIn())
            return

        rootView.progressBar.isIndeterminate = true

        compositeDisposable.add(authService.getProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    rootView.progressBar.isIndeterminate = false
                    rootView.progressBar.visibility = View.GONE
                }
                .subscribe({ user ->
                    Timber.d("Response Success")
                    rootView.name.text = "${user.firstName} ${user.lastName}"
                    rootView.email.text = user.email

                    Picasso.get()
                            .load(user.avatarUrl)
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .transform(CircleTransform())
                            .into(rootView.avatar)
                }) { throwable -> Timber.e(throwable, "Failure") })
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}
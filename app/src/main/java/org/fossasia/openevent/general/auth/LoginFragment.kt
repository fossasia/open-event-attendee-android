package org.fossasia.openevent.general.auth

import android.support.v4.app.Fragment
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel

class LoginFragment : Fragment() {

    private val loginActivityViewModel by viewModel<LoginFragmentViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_login, container, false)


        if (loginActivityViewModel.isLoggedIn())
            redirectToMain()

        rootView.loginButton.setOnClickListener {
            loginActivityViewModel.login(username.text.toString(), password.text.toString())
        }

        loginActivityViewModel.progress.observe(this, Observer {
            it?.let { showProgress(it) }
        })

        loginActivityViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        loginActivityViewModel.loggedIn.observe(this, Observer {
            Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show()
            redirectToMain()
        })

        return rootView
    }

    private fun redirectToMain() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun showProgress(enabled: Boolean) {
        loginButton.isEnabled = !enabled
        progressBar.visibility = if (enabled) View.VISIBLE else View.GONE
    }
}
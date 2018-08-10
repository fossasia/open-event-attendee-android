package org.fossasia.openevent.general.auth

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.ticket.EVENT_ID
import org.fossasia.openevent.general.ticket.TICKET_ID_AND_QTY
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel

const val LAUNCH_ATTENDEE: String = "LAUNCH_ATTENDEE"
class LoginFragment : Fragment() {

    private val loginActivityViewModel by viewModel<LoginFragmentViewModel>()
    private lateinit var rootView: View
    private var bundle: Bundle? = null
    private var ticketIdAndQty: List<Pair<Int, Int>>? = null
    private var id: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
            ticketIdAndQty = bundle.getSerializable(TICKET_ID_AND_QTY) as List<Pair<Int, Int>>
        }
        this.bundle = bundle
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_login, container, false)

        if (loginActivityViewModel.isLoggedIn())
            redirectToMain(bundle)

        rootView.loginButton.setOnClickListener {
            loginActivityViewModel.login(email.text.toString(), password.text.toString())
        }

        loginActivityViewModel.progress.observe(this, Observer {
            it?.let {
                Utils.showProgressBar(rootView.progressBar, it)
                loginButton.isEnabled = !it
            }
        })

        loginActivityViewModel.showNoInternetDialog.observe(this, Observer {
            Utils.showNoInternetDialog(context)
        })

        loginActivityViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        loginActivityViewModel.loggedIn.observe(this, Observer {
            Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show()
            loginActivityViewModel.fetchProfile()
        })

        rootView.email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(email: CharSequence, start: Int, before: Int, count: Int) {
                if (loginActivityViewModel.isCorrectEmail(email.toString())) {
                    rootView.forgotPassword.visibility = View.VISIBLE
                    rootView.loginButton.isEnabled = true
                } else {
                    rootView.forgotPassword.visibility = View.GONE
                    rootView.loginButton.isEnabled = false
                }
            }
        })

        loginActivityViewModel.requestTokenSuccess.observe(this, Observer {
            rootView.sentEmailLayout.visibility = View.VISIBLE
            rootView.loginLayout.visibility = View.GONE
        })

        rootView.tick.setOnClickListener {
            rootView.sentEmailLayout.visibility = View.GONE
            rootView.loginLayout.visibility = View.VISIBLE
        }

        rootView.forgotPassword.setOnClickListener {
            loginActivityViewModel.sendResetPasswordEmail(email.text.toString())
        }

        loginActivityViewModel.user.observe(this, Observer {
            redirectToMain(bundle)
        })

        return rootView
    }

    private fun redirectToMain(bundle: Bundle?) {
        val intent = Intent(activity, MainActivity::class.java)
        if (((bundle != null) && !id.equals(-1)) && (ticketIdAndQty != null)) {
            intent.putExtra(LAUNCH_ATTENDEE, true)
            intent.putExtras(bundle)
        }
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        activity?.finish()
    }
}
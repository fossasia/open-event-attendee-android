package org.fossasia.openevent.general.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_login.email
import kotlinx.android.synthetic.main.fragment_login.loginButton
import kotlinx.android.synthetic.main.fragment_login.password
import kotlinx.android.synthetic.main.fragment_login.view.email
import kotlinx.android.synthetic.main.fragment_login.view.forgotPassword
import kotlinx.android.synthetic.main.fragment_login.view.loginButton
import kotlinx.android.synthetic.main.fragment_login.view.loginLayout
import kotlinx.android.synthetic.main.fragment_login.view.progressBar
import kotlinx.android.synthetic.main.fragment_login.view.sentEmailLayout
import kotlinx.android.synthetic.main.fragment_login.view.tick
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.order.LAUNCH_TICKETS
import org.fossasia.openevent.general.ticket.EVENT_ID
import org.fossasia.openevent.general.ticket.TICKET_ID_AND_QTY
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LAUNCH_ATTENDEE: String = "LAUNCH_ATTENDEE"
class LoginFragment : Fragment() {

    private val loginViewModel by viewModel<LoginViewModel>()
    private lateinit var rootView: View
    private var bundle: Bundle? = null
    private var ticketIdAndQty: List<Pair<Int, Int>>? = null
    private var id: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null && !bundle.getBoolean(LAUNCH_TICKETS)) {
            id = bundle.getLong(EVENT_ID, -1)
            ticketIdAndQty = bundle.getSerializable(TICKET_ID_AND_QTY) as List<Pair<Int, Int>>
        }
        this.bundle = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_login, container, false)

        if (loginViewModel.isLoggedIn())
            redirectToMain(bundle)

        rootView.loginButton.setOnClickListener {
            loginViewModel.login(email.text.toString(), password.text.toString())
            hideSoftKeyboard(context, rootView)
        }

        loginViewModel.progress
            .nonNull()
            .observe(this, Observer {
                rootView.progressBar.isVisible = it
                loginButton.isEnabled = !it
            })

        loginViewModel.showNoInternetDialog
            .nonNull()
            .observe(this, Observer {
                Utils.showNoInternetDialog(context)
            })

        loginViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(
                getActivity()?.findViewById(android.R.id.content)!!,
                it, Snackbar.LENGTH_LONG).show()
            })

        loginViewModel.loggedIn
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(
                getActivity()?.findViewById(android.R.id.content)!!,
                getString(R.string.welcome_back), Snackbar.LENGTH_LONG).show()
                loginViewModel.fetchProfile()
            })

        rootView.email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(email: CharSequence, start: Int, before: Int, count: Int) {
                loginViewModel.checkEmail(email.toString())
            }
        })

        loginViewModel.requestTokenSuccess
            .nonNull()
            .observe(this, Observer {
                rootView.sentEmailLayout.visibility = View.VISIBLE
                rootView.loginLayout.visibility = View.GONE
            })

        loginViewModel.isCorrectEmail
            .nonNull()
            .observe(this, Observer {
                onEmailEntered(it)
            })

        rootView.tick.setOnClickListener {
            rootView.sentEmailLayout.visibility = View.GONE
            rootView.loginLayout.visibility = View.VISIBLE
        }

        rootView.forgotPassword.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            loginViewModel.sendResetPasswordEmail(email.text.toString())
        }

        loginViewModel.user
            .nonNull()
            .observe(this, Observer {
                redirectToMain(bundle)
            })

        return rootView
    }

    private fun redirectToMain(bundle: Bundle?) {
        val intent = Intent(activity, MainActivity::class.java)
        if (bundle != null) {
            if (id != -1L && ticketIdAndQty != null) {
                intent.putExtra(LAUNCH_ATTENDEE, true)
            }
            intent.putExtras(bundle)
        }
        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        activity?.finish()
    }

    private fun onEmailEntered(enable: Boolean) {
        rootView.loginButton.isEnabled = enable
        rootView.forgotPassword.isVisible = enable
    }
}

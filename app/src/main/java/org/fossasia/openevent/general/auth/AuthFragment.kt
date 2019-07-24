package org.fossasia.openevent.general.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_auth.view.getStartedButton
import kotlinx.android.synthetic.main.fragment_auth.view.email
import kotlinx.android.synthetic.main.fragment_auth.view.emailLayout
import kotlinx.android.synthetic.main.fragment_auth.view.rootLayout
import kotlinx.android.synthetic.main.fragment_auth.view.skipTextView
import kotlinx.android.synthetic.main.fragment_auth.view.toolbar
import kotlinx.android.synthetic.main.fragment_auth.view.setting
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.ComplexBackPressFragment
import org.fossasia.openevent.general.PLAY_STORE_BUILD_FLAVOR
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_DETAIL_FRAGMENT
import org.fossasia.openevent.general.search.ORDER_COMPLETED_FRAGMENT
import org.fossasia.openevent.general.search.SEARCH_RESULTS_FRAGMENT
import org.fossasia.openevent.general.search.location.SEARCH_LOCATION_FRAGMENT
import org.fossasia.openevent.general.speakercall.SPEAKERS_CALL_FRAGMENT
import org.fossasia.openevent.general.ticket.TICKETS_FRAGMENT
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.extensions.setSharedElementEnterTransition
import org.fossasia.openevent.general.welcome.WELCOME_FRAGMENT
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthFragment : Fragment(), ComplexBackPressFragment {
    private lateinit var rootView: View
    private val authViewModel by viewModel<AuthViewModel>()
    private val safeArgs: AuthFragmentArgs by navArgs()
    private val smartAuthViewModel by sharedViewModel<SmartAuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.FLAVOR == PLAY_STORE_BUILD_FLAVOR) {
            smartAuthViewModel.requestCredentials(SmartAuthUtil.getCredentialsClient(requireActivity()))
            smartAuthViewModel.isCredentialStored
                .nonNull()
                .observe(this, Observer {
                    if (it) redirectToLogin()
                })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_auth, container, false)
        setSharedElementEnterTransition()
        setupToolbar()

        val progressDialog = progressDialog(context)

        val snackbarMessage = safeArgs.snackbarMessage
        if (!snackbarMessage.isNullOrEmpty()) rootView.snackbar(snackbarMessage)

        val email = safeArgs.email
        if (email != null) {
            rootView.email.setText(email)
        }

        rootView.skipTextView.isVisible = safeArgs.showSkipButton
        rootView.skipTextView.setOnClickListener {
            findNavController(rootView).navigate(
                AuthFragmentDirections.actionAuthToEventsPop()
            )
        }

        rootView.getStartedButton.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            if (!Patterns.EMAIL_ADDRESS.matcher(rootView.email.text.toString()).matches()) {
                rootView.emailLayout.error = getString(R.string.invalid_email_message)
                return@setOnClickListener
            }
            authViewModel.checkUser(rootView.email.text.toString())
        }

        rootView.email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*Do Nothing*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /*Do Nothing*/ }
            override fun afterTextChanged(s: Editable?) {
                rootView.emailLayout.error = null
            }
        })

        authViewModel.isUserExists
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it)
                    redirectToLogin(rootView.email.text.toString())
                else
                    redirectToSignUp()
                authViewModel.mutableStatus.postValue(null)
            })

        authViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        smartAuthViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        authViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.rootLayout.longSnackbar(it)
            })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rootView.email.viewTreeObserver.addOnGlobalLayoutListener {
            startPostponedEnterTransition()
        }
    }

    private fun setupToolbar() {
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        rootView.setting.setOnClickListener {
            findNavController(rootView).navigate(AuthFragmentDirections.actionAuthToSetting())
        }
    }

    private fun redirectToLogin(email: String = "") {
        findNavController(rootView).navigate(AuthFragmentDirections.actionAuthToLogIn(email, safeArgs.redirectedFrom),
            FragmentNavigatorExtras(rootView.email to "emailLoginTransition"))
    }

    private fun redirectToSignUp() {
        findNavController(rootView).navigate(AuthFragmentDirections
            .actionAuthToSignUp(rootView.email.text.toString(), safeArgs.redirectedFrom),
                FragmentNavigatorExtras(rootView.email to "emailSignUpTransition"))
    }

    override fun handleBackPress() {
        when (safeArgs.redirectedFrom) {
            TICKETS_FRAGMENT -> findNavController(rootView).popBackStack(R.id.ticketsFragment, false)
            EVENT_DETAIL_FRAGMENT -> findNavController(rootView).popBackStack(R.id.eventDetailsFragment, false)
            WELCOME_FRAGMENT -> findNavController(rootView).popBackStack(R.id.welcomeFragment, false)
            SEARCH_LOCATION_FRAGMENT -> findNavController(rootView).popBackStack(R.id.searchLocationFragment, false)
            PROFILE_FRAGMENT -> findNavController(rootView).popBackStack(R.id.profileFragment, false)
            SEARCH_RESULTS_FRAGMENT -> findNavController(rootView).popBackStack(R.id.searchResultsFragment, false)
            ORDER_COMPLETED_FRAGMENT -> findNavController(rootView).popBackStack(R.id.orderCompletedFragment, false)
            SPEAKERS_CALL_FRAGMENT -> findNavController(rootView).popBackStack(R.id.speakersCallFragment, false)
            else -> findNavController(rootView).navigate(AuthFragmentDirections.actionAuthToEventsPop())
        }
    }
}

package org.fossasia.openevent.general.auth

import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_signup.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_DETAIL_FRAGMENT
import org.fossasia.openevent.general.favorite.FAVORITE_FRAGMENT
import org.fossasia.openevent.general.notification.NOTIFICATION_FRAGMENT
import org.fossasia.openevent.general.order.ORDERS_FRAGMENT
import org.fossasia.openevent.general.search.ORDER_COMPLETED_FRAGMENT
import org.fossasia.openevent.general.search.SEARCH_RESULTS_FRAGMENT
import org.fossasia.openevent.general.speakercall.SPEAKERS_CALL_FRAGMENT
import org.fossasia.openevent.general.ticket.TICKETS_FRAGMENT
import org.fossasia.openevent.general.utils.StringUtils.getTermsAndPolicyText
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.Utils.showNoInternetDialog
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.extensions.setSharedElementEnterTransition
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val MINIMUM_PASSWORD_LENGTH = 8

class SignUpFragment : Fragment() {

    private val signUpViewModel by viewModel<SignUpViewModel>()
    private val safeArgs: SignUpFragmentArgs by navArgs()
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_signup, container, false)
        setSharedElementEnterTransition()

        val progressDialog = progressDialog(context)
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        rootView.mainView.setOnClickListener {
            hideSoftKeyboard(context, rootView.mainView)
        }

        rootView.signUpText.text = getTermsAndPolicyText(requireContext(), resources)
        rootView.signUpText.movementMethod = LinkMovementMethod.getInstance()
        rootView.emailSignUp.text = SpannableStringBuilder(safeArgs.email)

        rootView.lastNameText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if (validateRequiredFieldsEmpty()) {
                        rootView.signUpButton.performClick()
                    }
                    hideSoftKeyboard(context, rootView)
                    true
                }
                else -> false
            }
        }

        rootView.signUpButton.setOnClickListener {
            if (!rootView.signUpCheckbox.isChecked) {
                rootView.snackbar(R.string.accept_terms_and_conditions)
                return@setOnClickListener
            } else {
                val signUp = SignUp(
                    email = rootView.emailSignUp.text.toString(),
                    password = rootView.passwordSignUp.text.toString(),
                    firstName = rootView.firstNameText.text.toString(),
                    lastName = rootView.lastNameText.text.toString()
                )
                signUpViewModel.signUp(signUp)
            }
        }

        signUpViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        signUpViewModel.showNoInternetDialog
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                showNoInternetDialog(context)
            })

        signUpViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.signupNestedScrollView.longSnackbar(it)
            })

        signUpViewModel.loggedIn
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                redirectToMain()
            })

        rootView.emailSignUp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                if (text.toString() != safeArgs.email)
                    findNavController(rootView).navigate(SignUpFragmentDirections
                        .actionSignupToAuthPop(redirectedFrom = safeArgs.redirectedFrom, email = text.toString()))
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Do Nothing*/ }
        })

        rootView.confirmPasswords.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                 */
                if (!rootView.textInputLayoutConfirmPassword.isEndIconVisible) {
                    rootView.textInputLayoutConfirmPassword.isEndIconVisible = true
                }

                if (rootView.confirmPasswords.text.toString() == rootView.passwordSignUp.text.toString()) {
                    rootView.textInputLayoutConfirmPassword.error = null
                    rootView.textInputLayoutConfirmPassword.isErrorEnabled = false
                } else {
                    rootView.textInputLayoutConfirmPassword.error = getString(R.string.invalid_confirm_password_message)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                rootView.signUpButton.isEnabled = checkPassword()
            }
        })

        rootView.passwordSignUp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                */
                if (!rootView.textInputLayoutPassword.isEndIconVisible) {
                    rootView.textInputLayoutPassword.isEndIconVisible = true
                }

                if (rootView.passwordSignUp.text.toString().length >= MINIMUM_PASSWORD_LENGTH) {
                    rootView.textInputLayoutPassword.error = null
                    rootView.textInputLayoutPassword.isErrorEnabled = false
                } else {
                    rootView.textInputLayoutPassword.error = getString(R.string.invalid_password_message)
                }
                if (rootView.confirmPasswords.text.toString() == rootView.passwordSignUp.text.toString()) {
                    rootView.textInputLayoutConfirmPassword.error = null
                    rootView.textInputLayoutConfirmPassword.isErrorEnabled = false
                } else {
                    rootView.textInputLayoutConfirmPassword.error = getString(R.string.invalid_confirm_password_message)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                rootView.signUpButton.isEnabled = checkPassword()
            }
        })

        return rootView
    }

    private fun redirectToMain() {
        val destinationId =
            when (safeArgs.redirectedFrom) {
                PROFILE_FRAGMENT -> R.id.profileFragment
                EVENT_DETAIL_FRAGMENT -> R.id.eventDetailsFragment
                ORDERS_FRAGMENT -> R.id.orderUnderUserFragment
                TICKETS_FRAGMENT -> R.id.ticketsFragment
                NOTIFICATION_FRAGMENT -> R.id.notificationFragment
                SPEAKERS_CALL_FRAGMENT -> R.id.speakersCallFragment
                FAVORITE_FRAGMENT -> R.id.favoriteFragment
                SEARCH_RESULTS_FRAGMENT -> R.id.searchResultsFragment
                ORDER_COMPLETED_FRAGMENT -> R.id.orderCompletedFragment
                else -> R.id.eventsFragment
            }
        if (destinationId == R.id.eventsFragment) {
            findNavController(rootView).navigate(SignUpFragmentDirections.actionSignUpToEventsPop())
        } else {
            findNavController(rootView).popBackStack(destinationId, false)
        }
        rootView.snackbar(R.string.logged_in_automatically)
    }

    private fun checkPassword() =
        rootView.passwordSignUp.text.toString().isNotEmpty() &&
        rootView.passwordSignUp.text.toString().length >= MINIMUM_PASSWORD_LENGTH &&
        rootView.passwordSignUp.text.toString() == rootView.confirmPasswords.text.toString()

    private fun validateRequiredFieldsEmpty(): Boolean {

        var status = true

        if (rootView.emailSignUp.text.isNullOrEmpty()) {
            rootView.emailSignUp.error = getString(R.string.empty_email_message)
            status = false
        }
        if (rootView.passwordSignUp.text.isNullOrEmpty()) {
            rootView.passwordSignUp.error = getString(R.string.empty_password_message)
            // make PasswordToggle invisible
            rootView.textInputLayoutPassword.isEndIconVisible = false
            status = false
        }
        if (rootView.confirmPasswords.text.isNullOrEmpty()) {
            rootView.confirmPasswords.error = getString(R.string.empty_confirm_password_message)
            // make PasswordToggle invisible
            rootView.textInputLayoutConfirmPassword.isEndIconVisible = false
            status = false
        }
        return status
    }
}

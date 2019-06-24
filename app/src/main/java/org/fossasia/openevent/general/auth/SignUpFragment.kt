package org.fossasia.openevent.general.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import kotlinx.android.synthetic.main.fragment_signup.view.textInputLayoutPassword
import kotlinx.android.synthetic.main.fragment_signup.view.textInputLayoutConfirmPassword
import kotlinx.android.synthetic.main.fragment_signup.view.firstNameText
import kotlinx.android.synthetic.main.fragment_signup.view.signUpButton
import kotlinx.android.synthetic.main.fragment_signup.view.lastNameText
import kotlinx.android.synthetic.main.fragment_signup.view.passwordSignUp
import kotlinx.android.synthetic.main.fragment_signup.view.confirmPasswords
import kotlinx.android.synthetic.main.fragment_signup.view.emailSignUp
import kotlinx.android.synthetic.main.fragment_signup.view.signupNestedScrollView
import kotlinx.android.synthetic.main.fragment_signup.view.signUpText
import kotlinx.android.synthetic.main.fragment_signup.view.signUpCheckbox
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.navigation.fragment.navArgs
import org.fossasia.openevent.general.event.EVENT_DETAIL_FRAGMENT
import org.fossasia.openevent.general.notification.NOTIFICATION_FRAGMENT
import org.fossasia.openevent.general.order.ORDERS_FRAGMENT
import org.fossasia.openevent.general.speakercall.SPEAKERS_CALL_FRAGMENT
import org.fossasia.openevent.general.ticket.TICKETS_FRAGMENT
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar

private const val MINIMUM_PASSWORD_LENGTH = 8

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

        val progressDialog = Utils.progressDialog(context)
        Utils.setToolbar(activity, getString(R.string.sign_up))
        setHasOptionsMenu(true)

        val paragraph = SpannableStringBuilder()
        val startText = getString(R.string.start_text)
        val termsText = getString(R.string.terms_text)
        val middleText = getString(R.string.middle_text)
        val privacyText = getString(R.string.privacy_text)

        paragraph.append(startText)
        paragraph.append(" $termsText")
        paragraph.append(" $middleText")
        paragraph.append(" $privacyText")

        val termsSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                Utils.openUrl(requireContext(), getString(R.string.terms_of_service))
            }
        }

        val privacyPolicySpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                Utils.openUrl(requireContext(), getString(R.string.privacy_policy))
            }
        }

        paragraph.setSpan(termsSpan, startText.length, startText.length + termsText.length + 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        paragraph.setSpan(privacyPolicySpan, paragraph.length - privacyText.length, paragraph.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // -1 so that we don't include "." in the link

        rootView.signUpText.text = paragraph
        rootView.signUpText.movementMethod = LinkMovementMethod.getInstance()
        rootView.emailSignUp.text = SpannableStringBuilder(safeArgs.email)

        rootView.lastNameText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if (validateRequiredFieldsEmpty()) {
                        rootView.signUpButton.performClick()
                    }
                    Utils.hideSoftKeyboard(context, rootView)
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
                Utils.showNoInternetDialog(context)
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

        signUpViewModel.areFieldsCorrect
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.signUpButton.isEnabled = it
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
                signUpViewModel.checkFields(rootView.emailSignUp.text.toString(),
                    rootView.passwordSignUp.text.toString(), rootView.confirmPasswords.text.toString())
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
                signUpViewModel.checkFields(rootView.emailSignUp.text.toString(),
                    rootView.passwordSignUp.text.toString(), rootView.confirmPasswords.text.toString())
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
                else -> R.id.eventsFragment
            }
        findNavController(rootView).popBackStack(destinationId, false)
        rootView.snackbar(R.string.logged_in_automatically)
    }

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

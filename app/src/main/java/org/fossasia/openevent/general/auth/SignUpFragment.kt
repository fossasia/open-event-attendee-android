package org.fossasia.openevent.general.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_signup.confirmPasswords
import kotlinx.android.synthetic.main.fragment_signup.firstNameText
import kotlinx.android.synthetic.main.fragment_signup.lastNameText
import kotlinx.android.synthetic.main.fragment_signup.passwordSignUp
import kotlinx.android.synthetic.main.fragment_signup.textInputLayoutPassword
import kotlinx.android.synthetic.main.fragment_signup.usernameSignUp
import kotlinx.android.synthetic.main.fragment_signup.signUpButton
import kotlinx.android.synthetic.main.fragment_signup.textInputLayoutConfirmPassword
import kotlinx.android.synthetic.main.fragment_signup.textInputLayoutEmail
import kotlinx.android.synthetic.main.fragment_signup.view.signUpButton
import kotlinx.android.synthetic.main.fragment_signup.view.lastNameText
import kotlinx.android.synthetic.main.fragment_signup.view.passwordSignUp
import kotlinx.android.synthetic.main.fragment_signup.view.confirmPasswords
import kotlinx.android.synthetic.main.fragment_signup.view.usernameSignUp
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

class SignUpFragment : Fragment() {

    private val signUpViewModel by viewModel<SignUpViewModel>()
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
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                Utils.openUrl(requireContext(), getString(R.string.terms_of_service))
            }
        }

        val privacyPolicySpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.isUnderlineText = false
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

        lateinit var confirmPassword: String
        val signUp = SignUp()

        rootView.lastNameText.setOnEditorActionListener { v, actionId, event ->
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
                Snackbar.make(rootView, R.string.accept_terms_and_conditions, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                signUp.email = usernameSignUp.text.toString()
                signUp.password = passwordSignUp.text.toString()
                signUp.firstName = firstNameText.text.toString()
                signUp.lastName = lastNameText.text.toString()
                confirmPassword = confirmPasswords.text.toString()
                signUpViewModel.signUp(signUp, confirmPassword)
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
                Snackbar.make(rootView.signupNestedScrollView, it, Snackbar.LENGTH_LONG).show()
            })

        signUpViewModel.signedUp
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(
                    rootView.signupNestedScrollView, R.string.sign_up_success, Snackbar.LENGTH_SHORT
                ).show()
                signUpViewModel.login(signUp)
            })

        signUpViewModel.loggedIn
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                redirectToMain()
            })

        signUpViewModel.areFieldsCorrect
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                signUpButton.isEnabled = it
            })

        rootView.usernameSignUp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (Patterns.EMAIL_ADDRESS.matcher(usernameSignUp.text.toString()).matches()) {
                    textInputLayoutEmail.error = null
                    textInputLayoutEmail.isErrorEnabled = false
                } else {
                    textInputLayoutEmail.error = getString(R.string.invalid_email_message)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpViewModel.checkFields(
                    usernameSignUp.text.toString(), passwordSignUp.text.toString(), confirmPasswords.text.toString())
            }
        })

        rootView.confirmPasswords.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                 */
                if (!textInputLayoutConfirmPassword.isEndIconVisible) {
                    textInputLayoutConfirmPassword.isEndIconVisible = true
                }

                if (confirmPasswords.text.toString() == passwordSignUp.text.toString()) {
                    textInputLayoutConfirmPassword.error = null
                    textInputLayoutConfirmPassword.isErrorEnabled = false
                } else {
                    textInputLayoutConfirmPassword.error = getString(R.string.invalid_confirm_password_message)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpViewModel.checkFields(
                    usernameSignUp.text.toString(), passwordSignUp.text.toString(), confirmPasswords.text.toString())
            }
        })

        rootView.passwordSignUp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                */
                if (!textInputLayoutPassword.isEndIconVisible) {
                    textInputLayoutPassword.isEndIconVisible = true
                }

                if (passwordSignUp.text.toString().length >= 6 || passwordSignUp.text.toString().isEmpty()) {
                    textInputLayoutPassword.error = null
                    textInputLayoutPassword.isErrorEnabled = false
                } else {
                    textInputLayoutPassword.error = getString(R.string.invalid_password_message)
                }
                if (confirmPasswords.text.toString() == passwordSignUp.text.toString()) {
                    textInputLayoutConfirmPassword.error = null
                    textInputLayoutConfirmPassword.isErrorEnabled = false
                } else {
                    textInputLayoutConfirmPassword.error = getString(R.string.invalid_confirm_password_message)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpViewModel.checkFields(
                    usernameSignUp.text.toString(), passwordSignUp.text.toString(), confirmPasswords.text.toString())
            }
        })

        return rootView
    }

    private fun redirectToMain() {
        findNavController(rootView).popBackStack()
        Snackbar.make(rootView, R.string.logged_in_automatically, Snackbar.LENGTH_SHORT).show()
    }

    private fun validateRequiredFieldsEmpty(): Boolean {

        var status = true

        if (usernameSignUp.text.isNullOrEmpty()) {
            usernameSignUp.error = getString(R.string.empty_email_message)
            status = false
        }
        if (passwordSignUp.text.isNullOrEmpty()) {
            passwordSignUp.error = getString(R.string.empty_password_message)
            // make PasswordToggle invisible
            textInputLayoutPassword.isEndIconVisible = false
            status = false
        }
        if (confirmPasswords.text.isNullOrEmpty()) {
            confirmPasswords.error = getString(R.string.empty_confirm_password_message)
            // make PasswordToggle invisible
            textInputLayoutConfirmPassword.isEndIconVisible = false
            status = false
        }
        return status
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController(rootView).popBackStack(R.id.eventsFragment, false)
                Snackbar.make(rootView, R.string.sign_in_canceled, Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

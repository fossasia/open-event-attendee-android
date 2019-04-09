package org.fossasia.openevent.general.settings

import androidx.appcompat.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_change_password.view.oldPassword
import kotlinx.android.synthetic.main.dialog_change_password.view.newPassword
import kotlinx.android.synthetic.main.dialog_change_password.view.confirmNewPassword
import kotlinx.android.synthetic.main.dialog_change_password.view.textInputLayoutNewPassword
import kotlinx.android.synthetic.main.dialog_change_password.view.textInputLayoutConfirmNewPassword
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull

class SettingsFragment : PreferenceFragmentCompat(), PreferenceChangeListener {
    private val FORM_LINK: String = "https://docs.google.com/forms/d/e/" +
        "1FAIpQLSd7Y1T1xoXeYaAG_b6Tu1YYK-jZssoC5ltmQbkUX0kmDZaKYw/viewform"
    private val PRIVACY_LINK: String = "https://eventyay.com/privacy-policy/"
    private val TERMS_OF_SERVICE_LINK: String = "https://eventyay.com/terms/"
    private val COOKIE_POLICY_LINK: String = "https://eventyay.com/cookie-policy/"
    private val WEBSITE_LINK: String = "https://eventyay.com/"
    private val settingsViewModel by viewModel<SettingsViewModel>()
    private val safeArgs: SettingsFragmentArgs by navArgs()

    override fun preferenceChange(evt: PreferenceChangeEvent?) {
        preferenceChange(evt)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey)
        val timeZonePreference = PreferenceManager.getDefaultSharedPreferences(context)

        setToolbar(activity, "Settings")
        setHasOptionsMenu(true)

        // Set Email
        preferenceScreen.findPreference(getString(R.string.key_profile))
            .summary = safeArgs.email

        // Set Build Version
        preferenceScreen.findPreference(getString(R.string.key_version))
            .title = "Version " + BuildConfig.VERSION_NAME

        preferenceScreen.findPreference(getString(R.string.key_timezone_switch))
            .setDefaultValue(timeZonePreference.getBoolean("useEventTimeZone", false))
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == getString(R.string.key_visit_website)) {
            // Goes to website
            Utils.openUrl(requireContext(), WEBSITE_LINK)
            return true
        }
        if (preference?.key == getString(R.string.key_rating)) {
            // Opens our app in play store
            startAppPlayStore(activity?.packageName.nullToEmpty())
            return true
        }
        if (preference?.key == getString(R.string.key_suggestion)) {
            // Links to suggestion form
            Utils.openUrl(requireContext(), FORM_LINK)
            return true
        }
        if (preference?.key == getString(R.string.key_change_password)) {
            // Links to suggestion form
            showChangePasswordDialog()
            return true
        }
        if (preference?.key == getString(R.string.key_timezone_switch)) {
            val timeZonePreference = PreferenceManager.getDefaultSharedPreferences(context)
            val timeZonePreferenceKey = "useEventTimeZone"
            when (timeZonePreference.getBoolean(timeZonePreferenceKey, false)) {
                true -> timeZonePreference.edit().putBoolean(timeZonePreferenceKey, false).apply()
                false -> timeZonePreference.edit().putBoolean(timeZonePreferenceKey, true).apply()
            }
        }
        if (preference?.key == getString(R.string.key_privacy)) {
            Utils.openUrl(requireContext(), PRIVACY_LINK)
            return true
        }
        if (preference?.key == getString(R.string.key_terms_of_service)) {
            Utils.openUrl(requireContext(), TERMS_OF_SERVICE_LINK)
            return true
        }
        if (preference?.key == getString(R.string.key_cookie_policy)) {
            Utils.openUrl(requireContext(), COOKIE_POLICY_LINK)
            return true
        }

        return false
    }

    private fun startAppPlayStore(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(settingsViewModel.getMarketAppLink(packageName))))
        } catch (error: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(settingsViewModel.getMarketWebLink(packageName))))
        }
    }

    private fun showChangePasswordDialog() {

        settingsViewModel.snackBar
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                view?.let { it1 -> Snackbar.make(it1, it, Snackbar.LENGTH_SHORT).show() }
            })

        val layout = layoutInflater.inflate(R.layout.dialog_change_password, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Change") { _, _ ->
                settingsViewModel.changePassword(layout.oldPassword.text.toString(), layout.newPassword.text.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        layout.newPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                */
                if (!layout.textInputLayoutNewPassword.isEndIconVisible) {
                    layout.textInputLayoutNewPassword.isEndIconVisible = true
                }

                if (layout.newPassword.text.toString().length >= 6) {
                    layout.textInputLayoutNewPassword.error = null
                    layout.textInputLayoutNewPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutNewPassword.error = getString(R.string.invalid_password_message)
                }
                if (layout.confirmNewPassword.text.toString() == layout.newPassword.text.toString()) {
                    layout.textInputLayoutConfirmNewPassword.error = null
                    layout.textInputLayoutConfirmNewPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutConfirmNewPassword.error =
                        getString(R.string.invalid_confirm_password_message)
                }
                when (layout.textInputLayoutConfirmNewPassword.isErrorEnabled ||
                    layout.textInputLayoutNewPassword.isErrorEnabled ||
                    layout.oldPassword.text.toString().length < 6) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })

        layout.confirmNewPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                 */
                if (!layout.textInputLayoutConfirmNewPassword.isEndIconVisible) {
                    layout.textInputLayoutConfirmNewPassword.isEndIconVisible = true
                }

                if (layout.confirmNewPassword.text.toString() == layout.newPassword.text.toString()) {
                    layout.textInputLayoutConfirmNewPassword.error = null
                    layout.textInputLayoutConfirmNewPassword.isErrorEnabled = false
                } else {
                    layout.textInputLayoutConfirmNewPassword.error =
                        getString(R.string.invalid_confirm_password_message)
                }
                when (layout.textInputLayoutConfirmNewPassword.isErrorEnabled ||
                    layout.textInputLayoutNewPassword.isErrorEnabled ||
                    layout.oldPassword.text.toString().length < 6) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })

        layout.oldPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                /* to make PasswordToggle visible again, if made invisible
                   after empty field error
                 */
                when (layout.textInputLayoutConfirmNewPassword.isErrorEnabled ||
                    layout.textInputLayoutNewPassword.isErrorEnabled ||
                    layout.oldPassword.text.toString().length < 6) {
                    true -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    false -> alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /*Implement here*/ }
        })
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

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}

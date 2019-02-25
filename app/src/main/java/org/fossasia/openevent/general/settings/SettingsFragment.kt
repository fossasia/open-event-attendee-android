package org.fossasia.openevent.general.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener

class SettingsFragment : PreferenceFragmentCompat(), PreferenceChangeListener {
    private var email: String? = null
    private val EMAIL: String = "EMAIL"
    private val FORM_LINK: String = "https://docs.google.com/forms/d/e/" +
        "1FAIpQLSd7Y1T1xoXeYaAG_b6Tu1YYK-jZssoC5ltmQbkUX0kmDZaKYw/viewform"
    private val PRIVACY_LINK: String = "https://eventyay.com/privacy-policy/"
    private val TERMS_OF_SERVICE_LINK: String = "https://eventyay.com/terms/"
    private val COOKIE_POLICY_LINK: String = "https://eventyay.com/cookie-policy/"
    private val settingsViewModel by viewModel<SettingsViewModel>()

    override fun preferenceChange(evt: PreferenceChangeEvent?) {
        preferenceChange(evt)
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey)

        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Settings"
        setHasOptionsMenu(true)

        // Set Email
        email = arguments?.getString(EMAIL)
        preferenceScreen.findPreference(resources.getString(R.string.key_profile))
            .summary = email

        // Set Build Version
        preferenceScreen.findPreference(resources.getString(R.string.key_version))
            .title = "Version " + BuildConfig.VERSION_NAME
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == resources.getString(R.string.key_rating)) {
            // Opens our app in play store
            startAppPlayStore(activity?.packageName.nullToEmpty())
            return true
        }
        if (preference?.key == resources.getString(R.string.key_suggestion)) {
            // Links to suggestion form
            context?.let {
                Utils.openUrl(it, FORM_LINK)
            }
            return true
        }
        if (preference?.key == getString(R.string.key_privacy)) {
            context?.let { Utils.openUrl(it, PRIVACY_LINK) }
            return true
        }
        if (preference?.key == getString(R.string.key_terms_of_service)) {
            context?.let { Utils.openUrl(it, TERMS_OF_SERVICE_LINK) }
            return true
        }
        if (preference?.key == getString(R.string.key_cookie_policy)) {
            context?.let { Utils.openUrl(it, COOKIE_POLICY_LINK) }
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

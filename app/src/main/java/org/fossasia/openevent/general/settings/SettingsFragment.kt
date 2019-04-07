package org.fossasia.openevent.general.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import org.fossasia.openevent.general.utils.Utils.setToolbar

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
        preferenceScreen.findPreference(resources.getString(R.string.key_profile))
            .summary = safeArgs.email

        // Set Build Version
        preferenceScreen.findPreference(resources.getString(R.string.key_version))
            .title = "Version " + BuildConfig.VERSION_NAME

        preferenceScreen.findPreference(resources.getString(R.string.key_timezone_switch))
            .setDefaultValue(timeZonePreference.getBoolean("useEventTimeZone", false))
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == resources.getString(R.string.key_visit_website)) {
            // Goes to website
            Utils.openUrl(requireContext(), WEBSITE_LINK)
            return true
        }
        if (preference?.key == resources.getString(R.string.key_rating)) {
            // Opens our app in play store
            startAppPlayStore(activity?.packageName.nullToEmpty())
            return true
        }
        if (preference?.key == resources.getString(R.string.key_suggestion)) {
            // Links to suggestion form
            Utils.openUrl(requireContext(), FORM_LINK)
            return true
        }
        if (preference?.key == resources.getString(R.string.key_timezone_switch)) {
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

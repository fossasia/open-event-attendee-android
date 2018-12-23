package org.fossasia.openevent.general.settings

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.view.MenuItem
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener

class SettingsFragment : PreferenceFragmentCompat(), PreferenceChangeListener {
    private var email: String? = null
    val EMAIL: String = "EMAIL"
    val FORM_LINK: String = "https://docs.google.com/forms/d/e/1FAIpQLSd7Y1T1xoXeYaAG_b6Tu1YYK-jZssoC5ltmQbkUX0kmDZaKYw/viewform"
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
        preferenceScreen.findPreference(resources.getString(R.string.key_profile)).summary = email

        // Set Build Version
        preferenceScreen.findPreference(resources.getString(R.string.key_version)).title = "Version " + BuildConfig.VERSION_NAME
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
        if (preference?.key == resources.getString(R.string.key_profile)) {
            // Logout Dialog shown
            showDialog()
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

    private fun showDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(resources.getString(R.string.message))
                .setPositiveButton(resources.getString(R.string.logout)) { _, _ ->
                    if (settingsViewModel.isLoggedIn()) {
                        settingsViewModel.logout()
                        startActivity(Intent(context, MainActivity::class.java))
                        activity?.finish()
                    }
                }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }
}

package org.fossasia.openevent.general.settings

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceScreen
import android.view.*
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.MainActivity
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

private const val EMAIL: String = "EMAIL"

class SettingsFragment : PreferenceFragmentCompat(), PreferenceChangeListener {
    private var email: String? = null
    private val settingsViewModel by viewModel<SettingsFragmentViewModel>()

    override fun preferenceChange(evt: PreferenceChangeEvent?) {
        preferenceChange(evt)
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey)
        val prefScreen: PreferenceScreen = preferenceScreen

        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Settings"
        setHasOptionsMenu(true)

        //Set Email
        email = arguments?.getString(EMAIL)
        prefScreen.findPreference(resources.getString(R.string.key_profile)).summary = email

        //Set Build Version
        prefScreen.findPreference(resources.getString(R.string.key_version)).title = "Version " + BuildConfig.VERSION_NAME
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == resources.getString(R.string.key_rating)) {
            //Open Orga app in play store
            startOrgaAppPlayStore(activity?.packageName)
            return true
        }
        if (preference?.key == resources.getString(R.string.key_suggestion)) {
            //Send feedback to email
            context?.let {
                openSuggestForm(it, resources.getString(R.string.suggestion_form_link))
            }
            return true
        }
        if (preference?.key == resources.getString(R.string.key_profile)) {
            //Show account
            showDialog()
            return true
        }
        return false
    }

    private fun startOrgaAppPlayStore(packageName: String?) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)))
        } catch (error: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)))
        }
    }

    private fun openSuggestForm(context: Context, url: String) {
        var finalUrl = url
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            finalUrl = "http://$url"
        }

        CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setCloseButtonIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_arrow_back_white_cct_24dp))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                .build()
                .launchUrl(context, Uri.parse(finalUrl))
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
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        activity?.supportActionBar?.title = "Profile"
        setHasOptionsMenu(false)
        super.onDestroyView()
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(resources.getString(R.string.message))
                .setPositiveButton(resources.getString(R.string.logout)) { _, _ ->
                    if(settingsViewModel.isLoggedIn()){
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
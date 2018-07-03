package org.fossasia.openevent.general.settings

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener

class LegalFragment : PreferenceFragmentCompat(), PreferenceChangeListener {
    val PRIVACY_LINK: String = "https://eventyay.com/privacy-policy/"
    val TERMS_OF_SERVICE_LINK: String = "https://eventyay.com/terms/"
    val COOKIE_POLICY_LINK: String = "https://eventyay.com/cookie-policy/"

    override fun preferenceChange(evt: PreferenceChangeEvent?) {
        preferenceChange(evt)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        context?.let { ContextCompat.getColor(it, android.R.color.white) }?.let { view?.setBackgroundColor(it) }
        return view
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.legal, rootKey)

        val activity =  activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Legal"
        setHasOptionsMenu(true)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == resources.getString(R.string.key_privacy)) {
            //Opens our app in play store
            context?.let {
                Utils.openUrl(it, PRIVACY_LINK)
            }
            return true
        }
        if (preference?.key == resources.getString(R.string.key_terms_of_service)) {
            //Links to suggestion form
            context?.let {
                Utils.openUrl(it, TERMS_OF_SERVICE_LINK)
            }
            return true
        }
        if (preference?.key == resources.getString(R.string.key_cookie_policy)) {
            //Logout Dialog shown
            context?.let {
                Utils.openUrl(it, COOKIE_POLICY_LINK)
            }
            return true
        }
        return false
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
        val activity =  activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Settings"
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}
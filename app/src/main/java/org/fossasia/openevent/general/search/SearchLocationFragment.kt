package org.fossasia.openevent.general.search

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_search_location.search
import kotlinx.android.synthetic.main.fragment_search_location.view.locationProgressBar
import kotlinx.android.synthetic.main.fragment_search_location.view.search
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LOCATION_PERMISSION_REQUEST = 1000
var fromSearchFragment = false

class SearchLocationFragment : Fragment() {
    private lateinit var rootView: View
    private val searchLocationViewModel by viewModel<SearchLocationViewModel>()
    private val geoLocationUI = GeoLocationUI()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_location, container, false)

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = ""
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        rootView.locationProgressBar.visibility = View.GONE

        fromSearchFragment = arguments?.getBoolean(FROM_SEARCH) ?: false

        if (thisActivity is Activity) geoLocationUI.configure(thisActivity, rootView, searchLocationViewModel)

        rootView.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Do your search
                searchLocationViewModel.saveSearch(query)
                val startMainActivity = Intent(context, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                if (fromSearchFragment) {
                    val searchBundle = Bundle()
                    searchBundle.putBoolean(TO_SEARCH, true)
                    startMainActivity.putExtras(searchBundle)
                }
                startActivity(startMainActivity)
                activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                activity?.finish()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        return rootView
    }

    override fun onResume() {
        search.requestFocus()
        context?.let {
            Utils.showKeyboard(it)
        }
        super.onResume()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val thisActivity = activity
                    if (thisActivity is Activity)
                        geoLocationUI.configure(thisActivity, rootView, searchLocationViewModel)
                } else
                    Snackbar.make(rootView, "Cannot fetch location!", Snackbar.LENGTH_SHORT).show()
            }
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
}

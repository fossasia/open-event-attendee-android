package org.fossasia.openevent.general.search

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_search_location.*
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

private const val FROM_SEARCH: String = "FromSearchFragment"
private const val TO_SEARCH: String = "ToSearchFragment"

class SearchLocationActivity : AppCompatActivity() {

    private val searchLocationViewModel by viewModel<SearchLocationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_location)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.title = ""
        val bundle = intent.extras
        val fromSearchFragment = bundle?.getBoolean(FROM_SEARCH) ?: false
        if (BuildConfig.FLAVOR.equals("platstore")) {
            val autocompleteFragment = placeAutocompletefFragment as PlaceAutocompleteFragment
            autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    // Save Searched Place String
                    searchLocationViewModel.saveSearch(place.name as String)
                    val startMainActivity = Intent(this@SearchLocationActivity, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                    if (fromSearchFragment) {
                        val searchBundle = Bundle()
                        searchBundle.putBoolean(TO_SEARCH, true)
                        startMainActivity.putExtras(searchBundle)
                    }

                    startActivity(startMainActivity)
                }

                override fun onError(status: Status) {
                    Timber.d(status.statusMessage, "Failed getting location")

                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

package org.fossasia.openevent.general.search

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import android.os.Bundle
import org.fossasia.openevent.general.R
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_search_location.search
import kotlinx.android.synthetic.main.activity_search_location.locationProgressBar
import org.fossasia.openevent.general.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val FROM_SEARCH: String = "FromSearchFragment"
private const val TO_SEARCH: String = "ToSearchFragment"
const val LOCATION_PERMISSION_REQUEST = 1000

class SearchLocationActivity : AppCompatActivity() {

    private val searchLocationViewModel by viewModel<SearchLocationViewModel>()
    val geoLocationUI = GeoLocationUI()
    var fromSearchFragment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_location)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.title = ""
        val bundle = intent.extras
        locationProgressBar.visibility = View.GONE

        if (bundle != null) {
            fromSearchFragment = bundle.getBoolean(FROM_SEARCH)
        }
        geoLocationUI.configure(this, searchLocationViewModel)

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Do your search
                searchLocationViewModel.saveSearch(query)
                val startMainActivity = Intent(this@SearchLocationActivity, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                if (fromSearchFragment) {
                    val searchBundle = Bundle()
                    searchBundle.putBoolean(TO_SEARCH, true)
                    startMainActivity.putExtras(searchBundle)
                }

                startActivity(startMainActivity)

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    geoLocationUI.configure(this, searchLocationViewModel)
               } else {
                    Toast.makeText(applicationContext, "Cannot fetch location", Toast.LENGTH_SHORT).show()
                }
            }
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

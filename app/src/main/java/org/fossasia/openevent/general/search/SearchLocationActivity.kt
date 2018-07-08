package org.fossasia.openevent.general.search

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.fossasia.openevent.general.R

import android.support.v7.widget.SearchView
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_search_location.*
import org.fossasia.openevent.general.MainActivity
import org.koin.android.architecture.ext.viewModel

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
        var fromSearchFragment = false

        if (bundle != null) {
            fromSearchFragment = bundle.getBoolean(FROM_SEARCH)
        }

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //Do your search
                searchLocationViewModel.saveSearch(query)
                val startMainActivity = Intent(this@SearchLocationActivity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

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

package org.fossasia.openevent.general.search

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import kotlinx.android.synthetic.fdroid.activity_search_location.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.androidx.viewmodel.ext.android.viewModel

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

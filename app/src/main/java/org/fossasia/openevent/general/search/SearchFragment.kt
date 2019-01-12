package org.fossasia.openevent.general.search

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_search.view.fabSearch
import kotlinx.android.synthetic.main.fragment_search.view.locationTextView
import kotlinx.android.synthetic.main.fragment_search.view.timeTextView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.SearchResultsActivity
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.view.MenuItemCompat
import org.fossasia.openevent.general.MainActivity

private const val FROM_SEARCH: String = "FromSearchFragment"
const val QUERY: String = "query"
const val LOCATION: String = "location"
const val DATE: String = "date"

class SearchFragment : Fragment() {
    private val searchViewModel by viewModel<SearchViewModel>()
    private lateinit var rootView: View
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_search, container, false)

        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.title = "Search"
        setHasOptionsMenu(true)

        rootView.timeTextView.setOnClickListener {
            val intent = Intent(activity, SearchTimeActivity::class.java)
            startActivity(intent)
        }

        if (searchViewModel.savedDate != null) {
            rootView.timeTextView.text = searchViewModel.savedDate
        }

        if (searchViewModel.savedLocation != null) {
            rootView.locationTextView.text = searchViewModel.savedLocation
        }

        rootView.locationTextView.setOnClickListener {
            val intent = Intent(activity, SearchLocationActivity::class.java)
            val bundle = Bundle()
            bundle.putBoolean(FROM_SEARCH, true)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_item -> {
                false
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val searchItem = menu.findItem(R.id.search_item)
        val thisActivity = activity
        if (thisActivity is MainActivity) searchView = SearchView(thisActivity.supportActionBar?.themedContext)
        MenuItemCompat.setActionView(searchItem, searchView)
        if(!searchViewModel.queryText.value.isNullOrEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(searchViewModel.queryText.value,false)
            searchView.maxWidth = Resources.getSystem().displayMetrics.widthPixels
        }
        val queryListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val intent = Intent(activity, SearchResultsActivity::class.java)
                intent.putExtra(QUERY, query)
                intent.putExtra(LOCATION, rootView.locationTextView.text.toString().nullToEmpty())
                intent.putExtra(DATE, rootView.timeTextView.text.toString().nullToEmpty())
                startActivity(intent)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchViewModel.queryText.value = newText
                return false
            }
        }
        searchView.setOnQueryTextListener(queryListener)
        rootView.fabSearch.setOnClickListener {
            queryListener.onQueryTextSubmit(searchView.query.toString())
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::searchView.isInitialized)
            searchView.setOnQueryTextListener(null)
    }
}

package org.fossasia.openevent.general.search

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
import androidx.navigation.Navigation.findNavController
import kotlinx.android.synthetic.main.fragment_search.view.fabSearch
import kotlinx.android.synthetic.main.fragment_search.view.locationTextView
import kotlinx.android.synthetic.main.fragment_search.view.timeTextView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.Navigation
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.utils.Utils.getAnimSlide

const val FROM_SEARCH: String = "FromSearchFragment"
const val QUERY: String = "query"
const val LOCATION: String = "location"
const val DATE: String = "date"
const val SEARCH_TIME: String = "time"

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

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = "Search"
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
        setHasOptionsMenu(true)

        rootView.timeTextView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(SEARCH_TIME, rootView.timeTextView.text.toString())
            Navigation.findNavController(rootView).navigate(R.id.searchTimeFragment, bundle, getAnimSlide())
        }

        val time = arguments?.let {
            SearchFragmentArgs.fromBundle(it).stringSavedDate
        }
        rootView.timeTextView.text = time ?: "Anytime"

        searchViewModel.loadSavedLocation()
        rootView.locationTextView.text = searchViewModel.savedLocation

        rootView.locationTextView.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean(FROM_SEARCH, true)
            Navigation.findNavController(rootView).navigate(R.id.searchLocationFragment, bundle, getAnimSlide())
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
        searchItem.actionView = searchView
        val queryListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val bundle = Bundle()
                bundle.putString(QUERY, query)
                bundle.putString(LOCATION, rootView.locationTextView.text.toString().nullToEmpty())
                bundle.putString(DATE, rootView.timeTextView.text.toString().nullToEmpty())
                findNavController(rootView).navigate(R.id.searchResultsFragment, bundle, getAnimSlide())
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        }
        searchView.setOnQueryTextListener(queryListener)
        rootView.fabSearch.setOnClickListener {
            queryListener.onQueryTextSubmit(searchView.query.toString())
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.isSaveEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::searchView.isInitialized)
            searchView.setOnQueryTextListener(null)
    }
}

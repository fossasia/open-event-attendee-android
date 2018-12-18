package org.fossasia.openevent.general.search

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.SearchResultsActivity
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel

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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.getItemId()) {
            R.id.search_item -> {
                return false
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.setGroupVisible(R.id.search_menu, true)
        menu.setGroupVisible(R.id.profile_menu, false)

        searchView = menu.findItem(R.id.search_item).actionView as SearchView
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

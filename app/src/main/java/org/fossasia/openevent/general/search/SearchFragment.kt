package org.fossasia.openevent.general.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_search.view.fabSearch
import kotlinx.android.synthetic.main.fragment_search.view.locationTextView
import kotlinx.android.synthetic.main.fragment_search.view.timeTextView
import kotlinx.android.synthetic.main.fragment_search.view.eventTypeTextView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.Navigation.findNavController
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.event.EventUtils.getFormattedDate
import org.fossasia.openevent.general.event.EventUtils.getFormattedDateWithoutYear
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Calendar
import org.fossasia.openevent.general.utils.Utils.setToolbar

const val SEARCH_FRAGMENT = "SearchFragment"

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

        setToolbar(activity, getString(R.string.search), false)
        setHasOptionsMenu(true)

        rootView.timeTextView.setOnClickListener {
            findNavController(rootView).navigate(SearchFragmentDirections.actionSearchToSearchTime(
                rootView.timeTextView.text.toString(),
                fromFragmentName = SEARCH_FRAGMENT
            ))
        }
        searchViewModel.loadSavedTime()
        val time = searchViewModel.savedTime
        if (time.isNullOrBlank()) rootView.timeTextView.text = getString(R.string.anytime)
        else {
            try {
                val zonedDate =
                    LocalDate.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        .atStartOfDay(ZoneId.systemDefault()).plusDays(-1)
                if (zonedDate.year == Calendar.getInstance().get(Calendar.YEAR))
                    rootView.timeTextView.text = getFormattedDateWithoutYear(zonedDate)
                else rootView.timeTextView.text = getFormattedDate(zonedDate)
            } catch (e: DateTimeParseException) {
                rootView.timeTextView.text = time
            }
        }
        searchViewModel.loadSavedType()
        val type = searchViewModel.savedType
        rootView.eventTypeTextView.text = if (type.isNullOrBlank()) getString(R.string.anything) else type

        searchViewModel.loadSavedLocation()
        rootView.locationTextView.text = searchViewModel.savedLocation

        rootView.locationTextView.setOnClickListener {
            findNavController(rootView).navigate(SearchFragmentDirections.actionSearchToSearchLocation(
                fromFragmentName = SEARCH_FRAGMENT
            ))
        }

        rootView.eventTypeTextView.setOnClickListener {
            findNavController(rootView).navigate(SearchFragmentDirections.actionSearchToSearchType(
                fromFragmentName = SEARCH_FRAGMENT
            ))
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
        searchView.maxWidth = Int.MAX_VALUE
        searchItem.actionView = searchView
        val queryListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                findNavController(rootView).navigate(SearchFragmentDirections.actionSearchToSearchResults(
                    query = query,
                    location = rootView.locationTextView.text.toString().nullToEmpty(),
                    date = (searchViewModel.savedTime ?: getString(R.string.anytime)).nullToEmpty(),
                    type = (searchViewModel.savedType ?: getString(R.string.anything)).nullToEmpty()
                ))
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

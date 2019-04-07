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
import androidx.navigation.Navigation.findNavController
import kotlinx.android.synthetic.main.fragment_search.view.fabSearch
import kotlinx.android.synthetic.main.fragment_search.view.locationTextView
import kotlinx.android.synthetic.main.fragment_search.view.timeTextView
import kotlinx.android.synthetic.main.fragment_search.view.eventTypeTextView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.Navigation
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.event.EventUtils.getFormattedDate
import org.fossasia.openevent.general.event.EventUtils.getFormattedDateWithoutYear
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Calendar
import org.fossasia.openevent.general.utils.Utils.setToolbar

class SearchFragment : Fragment() {
    private val searchViewModel by viewModel<SearchViewModel>()
    private val safeArgs: SearchFragmentArgs? by lazy {
        // When search fragment is opened using BottomNav, then fragment arguments are null
        // navArgs delegate throws an IllegalStateException when arguments are null, so we construct SearchFragmentArgs
        // from the arguments bundle
        arguments?.let { SearchFragmentArgs.fromBundle(it) }
    }
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
            SearchTimeFragmentArgs.Builder()
                .setTime(rootView.timeTextView.text.toString())
                .build()
                .toBundle()
                .also { bundle ->
                    Navigation.findNavController(rootView).navigate(R.id.searchTimeFragment, bundle, getAnimSlide())
                }
        }

        val time = safeArgs?.stringSavedDate
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
        val type = safeArgs?.stringSavedType
        rootView.eventTypeTextView.text = if (type.isNullOrBlank()) getString(R.string.anything) else type

        searchViewModel.loadSavedLocation()
        rootView.locationTextView.text = searchViewModel.savedLocation

        rootView.locationTextView.setOnClickListener {
            SearchLocationFragmentArgs.Builder()
                .setFromSearchFragment(true)
                .build()
                .toBundle()
                .also { bundle ->
                    Navigation.findNavController(rootView).navigate(R.id.searchLocationFragment, bundle, getAnimSlide())
                }
        }

        rootView.eventTypeTextView.setOnClickListener {
            SearchLocationFragmentArgs.Builder()
                .setFromSearchFragment(true)
                .build()
                .toBundle()
                .also { bundle ->
                    Navigation.findNavController(rootView).navigate(R.id.searchTypeFragment, bundle, getAnimSlide())
                }
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
                SearchResultsFragmentArgs.Builder()
                    .setQuery(query)
                    .setLocation(rootView.locationTextView.text.toString().nullToEmpty())
                    .setDate((safeArgs?.stringSavedDate ?: getString(R.string.anytime)).nullToEmpty())
                    .setType((safeArgs?.stringSavedType ?: getString(R.string.anything)).nullToEmpty())
                    .build()
                    .toBundle()
                    .also { bundle ->
                        findNavController(rootView).navigate(R.id.searchResultsFragment, bundle, getAnimSlide())
                    }
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

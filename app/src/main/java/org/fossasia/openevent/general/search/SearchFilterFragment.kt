package org.fossasia.openevent.general.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_search_filter.view.dateRadioButton
import kotlinx.android.synthetic.main.fragment_search_filter.view.freeStuffCheckBox
import kotlinx.android.synthetic.main.fragment_search_filter.view.nameRadioButton
import kotlinx.android.synthetic.main.fragment_search_filter.view.radioGroup
import kotlinx.android.synthetic.main.fragment_search_filter.view.tvSelectCategory
import kotlinx.android.synthetic.main.fragment_search_filter.view.tvSelectDate
import kotlinx.android.synthetic.main.fragment_search_filter.view.tvSelectLocation
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.navigateWithBundleTo
import org.koin.androidx.viewmodel.ext.android.viewModel

const val SEARCH_FILTER_FRAGMENT = "SearchFilterFragment"

class SearchFilterFragment : Fragment() {
    private lateinit var rootView: View
    private var isFreeStuffChecked = false
    private lateinit var selectedTime: String
    private lateinit var selectedLocation: String
    private lateinit var selectedCategory: String
    private val searchViewModel by viewModel<SearchViewModel>()
    private val safeArgs: SearchFilterFragmentArgs by navArgs()
    private lateinit var sortBy: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setToolbar(activity)
        setHasOptionsMenu(true)
        rootView = inflater.inflate(R.layout.fragment_search_filter, container, false)
        setFilterParams()
        setFilters()
        setSortByRadioGroup()
        return rootView
    }

    private fun setSortByRadioGroup() {
        sortBy = safeArgs.sort
        if (sortBy == getString(R.string.sort_by_name)) {
            rootView.nameRadioButton.isChecked = true
        } else {
            rootView.dateRadioButton.isChecked = true
        }
        rootView.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = rootView.findViewById(checkedId)
            sortBy = if (radio.text == getString(R.string.sort_by_name)) {
                getString(R.string.sort_by_name)
            } else {
                getString(R.string.sort_by_date)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_filter, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.filter_set -> {
                findNavController(rootView).popBackStack(R.id.searchResultsFragment, true)
                SearchResultsFragmentArgs(
                    date = selectedTime,
                    freeEvents = isFreeStuffChecked,
                    location = selectedLocation,
                    type = selectedCategory,
                    query = safeArgs.query,
                    sort = sortBy
                )
                    .toBundle()
                    .navigateWithBundleTo(rootView, R.id.searchResultsFragment, getAnimFade())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setFilterParams() {
        with(searchViewModel) {
            loadSavedLocation()
            selectedLocation = savedLocation ?: getString(R.string.anywhere)
            loadSavedTime()
            selectedTime = savedTime ?: getString(R.string.anytime)
            loadSavedType()
            selectedCategory = savedType ?: getString(R.string.anything)
        }
    }
    private fun setFilters() {

        rootView.tvSelectDate.text = selectedTime
        rootView.tvSelectDate.setOnClickListener {
            SearchTimeFragmentArgs(
                time = selectedTime,
                fromFragmentName = SEARCH_FILTER_FRAGMENT,
                query = safeArgs.query
            )
                .toBundle()
                .navigateWithBundleTo(rootView, R.id.searchTimeFragment, getAnimFade())
        }

        rootView.tvSelectLocation.text = selectedLocation
        rootView.tvSelectLocation.setOnClickListener {
            SearchTimeFragmentArgs(
                time = selectedLocation,
                fromFragmentName = SEARCH_FILTER_FRAGMENT,
                query = safeArgs.query
            )
                .toBundle()
                .navigateWithBundleTo(rootView, R.id.searchLocationFragment, getAnimFade())
        }

        rootView.tvSelectCategory.text = selectedCategory
        rootView.tvSelectCategory.setOnClickListener {
            SearchTimeFragmentArgs(
                time = selectedCategory,
                fromFragmentName = SEARCH_FILTER_FRAGMENT,
                query = safeArgs.query
            )
                .toBundle()
                .navigateWithBundleTo(rootView, R.id.searchTypeFragment, getAnimFade())
        }

        rootView.freeStuffCheckBox.isChecked = safeArgs.freeEvents
        rootView.freeStuffCheckBox.setOnCheckedChangeListener { _, isChecked ->
            isFreeStuffChecked = isChecked
        }
    }
}

package org.fossasia.openevent.general.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_search_filter.view.callForSpeakerCheckBox
import kotlinx.android.synthetic.main.fragment_search_filter.view.dateRadioButton
import kotlinx.android.synthetic.main.fragment_search_filter.view.freeStuffCheckBox
import kotlinx.android.synthetic.main.fragment_search_filter.view.sessionsAndSpeakerCheckBox
import kotlinx.android.synthetic.main.fragment_search_filter.view.nameRadioButton
import kotlinx.android.synthetic.main.fragment_search_filter.view.radioGroup
import kotlinx.android.synthetic.main.fragment_search_filter.view.tvSelectCategory
import kotlinx.android.synthetic.main.fragment_search_filter.view.tvSelectDate
import kotlinx.android.synthetic.main.fragment_search_filter.view.tvSelectLocation
import kotlinx.android.synthetic.main.fragment_search_filter.view.toolbar
import kotlinx.android.synthetic.main.fragment_search_filter.view.toolbarLayout
import kotlinx.android.synthetic.main.fragment_search_filter.view.tick
import kotlinx.android.synthetic.main.fragment_search_filter.view.scrollView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val SEARCH_FILTER_FRAGMENT = "SearchFilterFragment"

class SearchFilterFragment : Fragment() {
    private lateinit var rootView: View
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
        rootView = inflater.inflate(R.layout.fragment_search_filter, container, false)
        setupToolbar()
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
        rootView.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = rootView.findViewById(checkedId)
            sortBy = if (radio.text == getString(R.string.sort_by_name)) {
                getString(R.string.sort_by_name)
            } else {
                getString(R.string.sort_by_date)
            }
        }
    }

    private fun setupToolbar() {
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        rootView.tick.setOnClickListener {
            findNavController(rootView).navigate(SearchFilterFragmentDirections.actionSearchFilterToSearchResults(
                date = selectedTime,
                freeEvents = rootView.freeStuffCheckBox.isChecked,
                location = selectedLocation,
                type = selectedCategory,
                query = safeArgs.query,
                sort = sortBy,
                sessionsAndSpeakers = rootView.sessionsAndSpeakerCheckBox.isChecked,
                callForSpeakers = rootView.callForSpeakerCheckBox.isChecked
            ))
        }
        rootView.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY > 0)
                rootView.toolbarLayout.elevation = resources.getDimension(R.dimen.custom_toolbar_elevation)
            else
                rootView.toolbarLayout.elevation = 0F
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
            findNavController(rootView).navigate(SearchFilterFragmentDirections.actionSearchFilterToSearchTime(
                time = selectedTime,
                fromFragmentName = SEARCH_FILTER_FRAGMENT,
                query = safeArgs.query
            ))
        }

        rootView.tvSelectLocation.text = selectedLocation
        rootView.tvSelectLocation.setOnClickListener {
            findNavController(rootView).navigate(SearchFilterFragmentDirections.actionSearchFilterToSearchLocation(
                fromFragmentName = SEARCH_FILTER_FRAGMENT,
                query = safeArgs.query
            ))
        }

        rootView.tvSelectCategory.text = selectedCategory
        rootView.tvSelectCategory.setOnClickListener {
            findNavController(rootView).navigate(SearchFilterFragmentDirections.actionSearchFilterToSearchType(
                selectedCategory,
                SEARCH_FILTER_FRAGMENT,
                safeArgs.query
            ))
        }

        rootView.freeStuffCheckBox.isChecked = safeArgs.freeEvents
        rootView.sessionsAndSpeakerCheckBox.isChecked = safeArgs.sessionsAndSpeakers
        rootView.callForSpeakerCheckBox.isChecked = safeArgs.callForSpeakers
    }
}

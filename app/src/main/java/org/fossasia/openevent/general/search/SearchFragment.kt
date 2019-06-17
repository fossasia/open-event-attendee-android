package org.fossasia.openevent.general.search

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_search.view.fabSearch
import kotlinx.android.synthetic.main.fragment_search.view.locationTextView
import kotlinx.android.synthetic.main.fragment_search.view.timeTextView
import kotlinx.android.synthetic.main.fragment_search.view.eventTypeTextView
import kotlinx.android.synthetic.main.fragment_search.view.searchText
import kotlinx.android.synthetic.main.fragment_search.view.searchInfoContainer
import kotlinx.android.synthetic.main.fragment_search.view.toolbar
import kotlinx.android.synthetic.main.fragment_search.view.backgroundImage
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.Navigation.findNavController
import com.squareup.picasso.Picasso
import org.fossasia.openevent.general.BottomIconDoubleClick
import org.fossasia.openevent.general.ComplexBackPressFragment
import org.fossasia.openevent.general.event.EventUtils.getFormattedDate
import org.fossasia.openevent.general.event.EventUtils.getFormattedDateWithoutYear
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Calendar
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.Utils.showSoftKeyboard

const val SEARCH_FRAGMENT = "SearchFragment"

class SearchFragment : Fragment(), ComplexBackPressFragment, BottomIconDoubleClick {
    private val searchViewModel by viewModel<SearchViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_search, container, false)
        setToolbar()

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

        if (searchViewModel.isQuerying)
            startQuerying()
        else
            stopQuerying()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rootView.fabSearch.setOnClickListener {
            makeSearch()
        }
        rootView.searchText.setOnClickListener {
            if (!searchViewModel.isQuerying)
                startQuerying()
        }
        rootView.searchText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER) {
                makeSearch()
                true
            } else {
                false
            }
        }
    }

    override fun handleBackPress() {
        if (searchViewModel.isQuerying)
            stopQuerying()
        else
            findNavController(rootView).popBackStack()
    }

    override fun doubleClick() {
        startQuerying()
    }

    private fun setToolbar() {
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            if (searchViewModel.isQuerying)
                stopQuerying()
            else
                startQuerying()
        }
    }

    private fun makeSearch() {
        searchViewModel.isQuerying = false
        findNavController(rootView).navigate(SearchFragmentDirections.actionSearchToSearchResults(
            query = rootView.searchText.text.toString(),
            location = rootView.locationTextView.text.toString().nullToEmpty(),
            date = (searchViewModel.savedTime ?: getString(R.string.anytime)).nullToEmpty(),
            type = (searchViewModel.savedType ?: getString(R.string.anything)).nullToEmpty()
        ))
        rootView.searchText.setText("")
    }

    private fun startQuerying() {
        searchViewModel.isQuerying = true
        rootView.searchInfoContainer.isVisible = false
        Picasso.get()
            .load(R.color.colorPrimary)
            .placeholder(R.color.colorPrimary)
            .into(rootView.backgroundImage)
        rootView.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_white_cct)
        rootView.searchText.requestFocus()
        showSoftKeyboard(context, rootView)
    }

    private fun stopQuerying() {
        searchViewModel.isQuerying = false
        rootView.searchInfoContainer.isVisible = true
        val background = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            R.drawable.background_fragment else R.drawable.background_fragment_search
        Picasso.get()
            .load(background)
            .placeholder(background)
            .into(rootView.backgroundImage)
        rootView.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_search_white)
        hideSoftKeyboard(context, rootView)
    }
}

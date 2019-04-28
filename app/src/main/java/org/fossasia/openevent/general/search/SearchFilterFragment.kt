package org.fossasia.openevent.general.search

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_search_filter.view.dateRadioButton
import kotlinx.android.synthetic.main.fragment_search_filter.view.freeStuffCheckBox
import kotlinx.android.synthetic.main.fragment_search_filter.view.nameRadioButton
import kotlinx.android.synthetic.main.fragment_search_filter.view.radioGroup
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.setToolbar

class SearchFilterFragment : Fragment() {
    private lateinit var rootView: View
    private var isFreeStuffChecked = false
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
        setFilterToolbar()
        setFreeStuffCheckBox()
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

    private fun setFilterToolbar() {
        val close = resources.getDrawable(R.drawable.ic_close)
        setBackIndicator(close)
    }

    private fun setBackIndicator(indicator: Drawable? = null) {
        (activity as? AppCompatActivity)?.supportActionBar?.setHomeAsUpIndicator(indicator)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                setBackIndicator()
                activity?.onBackPressed()
                true
            }
            R.id.filter_set -> {
                setBackIndicator()
                val navigator = Navigation.findNavController(rootView)
                navigator.popBackStack(R.id.searchResultsFragment, true)
                SearchFilterFragmentArgs(
                    date = safeArgs.date,
                    freeEvents = isFreeStuffChecked,
                    location = safeArgs.location,
                    sort = sortBy,
                    type = safeArgs.type,
                    query = safeArgs.query
                )
                    .toBundle()
                    .also {
                        navigator.navigate(R.id.searchResultsFragment, it, getAnimFade())
                    }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setFreeStuffCheckBox() {
        rootView.freeStuffCheckBox.isChecked = safeArgs.freeEvents
        rootView.freeStuffCheckBox.setOnCheckedChangeListener { _, isChecked ->
            isFreeStuffChecked = isChecked
        }
    }
}

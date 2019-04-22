package org.fossasia.openevent.general.search

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_search_filter.view.freeStuffCheckBox
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.setToolbar

class SearchFilterFragment : Fragment() {

    private lateinit var rootView: View
    private var isFreeStuffChecked = false
    private val safeArgs: SearchFilterFragmentArgs by navArgs()

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
        return rootView
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
                SearchFilterFragmentArgs.Builder()
                    .setDate(safeArgs.date)
                    .setFreeEvents(isFreeStuffChecked)
                    .setLocation(safeArgs.location)
                    .setType(safeArgs.type)
                    .setQuery(safeArgs.query)
                    .build()
                    .toBundle()
                    .also {
                        Navigation.findNavController(rootView)
                            .navigate(R.id.searchResultsFragment, it, getAnimFade())
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

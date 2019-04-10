package org.fossasia.openevent.general.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_search_type.view.eventTypesLv
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchTypeFragment : Fragment() {
    private val searchTypeViewModel by viewModel<SearchTypeViewModel>()
    private lateinit var rootView: View
    private val eventTypesList: MutableList<String> = arrayListOf("Anything")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search_type, container, false)
        setToolbar(activity, "", hasUpEnabled = true)
        setHasOptionsMenu(true)
        searchTypeViewModel.loadEventTypes()
        val adapter = ArrayAdapter(context, R.layout.event_type_list, eventTypesList)
        rootView.eventTypesLv.adapter = adapter

        searchTypeViewModel.eventTypes
            .nonNull()
            .observe(this, Observer { list ->
                list.forEach {
                    eventTypesList.add(it.name)
                }
                adapter.notifyDataSetChanged()
            })
        rootView.eventTypesLv.setOnItemClickListener { parent, view, position, id ->
            redirectToSearch(eventTypesList[position])
        }
        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun redirectToSearch(type: String) {
        searchTypeViewModel.saveType(type)
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.eventsFragment, false).build()
        Navigation.findNavController(rootView).navigate(R.id.searchFragment, null, navOptions)
    }
}

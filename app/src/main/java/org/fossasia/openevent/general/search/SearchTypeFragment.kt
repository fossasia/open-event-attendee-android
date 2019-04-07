package org.fossasia.openevent.general.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_search_type.view.eventTypesLv
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchTypeFragment : Fragment() {
    private val searchTypeViewModel by viewModel<SearchTypeViewModel>()
    private val safeArgs: SearchTypeFragmentArgs by navArgs()
    private lateinit var rootView: View
    private val eventTypesList: MutableList<String> = ArrayList()

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

        searchTypeViewModel.eventLocations
            .nonNull()
            .observe(this, Observer { list ->
                list.forEach {
                    eventTypesList.add(it.name ?: "")
                }
                adapter.notifyDataSetChanged()
            })
        rootView.eventTypesLv.setOnItemClickListener { parent, view, position, id ->
            redirectToSearch(eventTypesList[position])
        }
        return rootView
    }

    private fun redirectToSearch(type: String) {
        val args = SearchFragmentArgs.Builder().setStringSavedType(type).build().toBundle()
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.eventsFragment, false).build()
        Navigation.findNavController(rootView).navigate(R.id.searchFragment, args, navOptions)
    }
}

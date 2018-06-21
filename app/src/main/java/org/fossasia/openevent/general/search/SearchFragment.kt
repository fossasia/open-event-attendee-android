package org.fossasia.openevent.general.search

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventDetailsFragment
import org.fossasia.openevent.general.event.EventsRecyclerAdapter
import org.fossasia.openevent.general.event.RecyclerViewClickListener
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber


class SearchFragment : Fragment() {
    private val eventsRecyclerAdapter: EventsRecyclerAdapter = EventsRecyclerAdapter()
    private val searchViewModel by viewModel<SearchViewModel>()
    private lateinit var rootView: View
    private var loadEventsAgain=false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search, container, false)

        setHasOptionsMenu(true)

        rootView.progressBar.isIndeterminate = true

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.eventsRecycler.adapter = eventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(fragment.EVENT_ID, eventID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.frameContainer, fragment)?.addToBackStack(null)?.commit()
            }
        }
        eventsRecyclerAdapter.setListener(recyclerViewClickListener)
        searchViewModel.events.observe(this, Observer {
            it?.let {
                eventsRecyclerAdapter.addAll(it)
                eventsRecyclerAdapter.notifyDataSetChanged()
            }
            Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
        })

        searchViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        searchViewModel.progress.observe(this, Observer {
            it?.let { Utils.showProgressBar(rootView.progressBar, it) }
        })

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.getItemId()) {
            R.id.searchItem -> {
                return false
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.setGroupVisible(R.id.searchMenu, true)
        menu?.setGroupVisible(R.id.profileMenu, false)

        val searchView:SearchView ?= menu?.findItem(R.id.searchItem)?.actionView as? SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //Do your search
                searchViewModel.searchEvent = query
                if (!TextUtils.isEmpty(rootView.location_editText.text))
                    searchViewModel.location = rootView.location_editText.text.toString()
                rootView.search_linear_layout.visibility = View.GONE
                searchViewModel.loadEvents()
                loadEventsAgain = true
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        super.onPrepareOptionsMenu(menu)
    }

}
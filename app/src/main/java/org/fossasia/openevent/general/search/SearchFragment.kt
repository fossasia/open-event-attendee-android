package org.fossasia.openevent.general.search

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventDetailsFragment
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class SearchFragment : Fragment() {
    private val eventsRecyclerAdapter: SearchRecyclerAdapter = SearchRecyclerAdapter()
    private val searchViewModel by viewModel<SearchViewModel>()
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search, container, false)

        rootView.progressBar.isIndeterminate = true

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.eventsRecycler.adapter = eventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        rootView.eventsRecycler.layoutManager = linearLayoutManager

        val slideUp = SlideInUpAnimator()
        slideUp.addDuration = 500
        rootView.eventsRecycler.itemAnimator = slideUp

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(fragment.EVENT_ID, eventID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.frame_container, fragment)?.addToBackStack(null)?.commit()
            }
        }
        eventsRecyclerAdapter.setListener(recyclerViewClickListener)
        searchViewModel.events.observe(this, Observer {
            it?.let {
                eventsRecyclerAdapter.addAll(it)
            }
            notifyEventItems()
            Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
        })

        searchViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        searchViewModel.progress.observe(this, Observer {
            it?.let { showProgressBar(it) }
        })

        rootView.search_button.setOnClickListener({
            searchViewModel.searchEvent = rootView.search.text.toString()
            searchViewModel.loadEvents()
        })

        return rootView
    }

    private fun showProgressBar(show: Boolean) {
        rootView.progressBar.isIndeterminate = show
        rootView.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun notifyEventItems() {
        val firstVisible = linearLayoutManager.findFirstVisibleItemPosition()
        val lastVisible = linearLayoutManager.findLastVisibleItemPosition()
        val itemsChanged = lastVisible - firstVisible + 1 // + 1 because we start count items from 0
        val start = if (firstVisible - itemsChanged > 0) firstVisible - itemsChanged else 0
        eventsRecyclerAdapter.notifyItemRangeChanged(start, itemsChanged + itemsChanged)
    }
}
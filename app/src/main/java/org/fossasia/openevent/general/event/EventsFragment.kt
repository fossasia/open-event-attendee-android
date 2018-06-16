package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_events.view.*
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.view.inputmethod.InputMethodManager


class EventsFragment : Fragment() {
    private val eventsRecyclerAdapter: EventsRecyclerAdapter = EventsRecyclerAdapter()
    private val eventsViewModel by viewModel<EventsViewModel>()
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_events, container, false)

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
                /*
                if the user opens up the Event details fragment and then returns to the Events fragments,
                the slide up animation won't be shown again
                 */
                eventsViewModel.loadEventsWithAnimation = false
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.frame_container, fragment)?.addToBackStack(null)?.commit()
            }
        }
        eventsRecyclerAdapter.setListener(recyclerViewClickListener)
        eventsViewModel.events.observe(this, Observer {
            it?.let {
                eventsRecyclerAdapter.addAll(it)
            }
            notifyEventItems()
            Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
        })

        eventsViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        eventsViewModel.progress.observe(this, Observer {
            it?.let { showProgressBar(it) }
        })

        /*if (eventsViewModel.showAllEvents) {
            eventsViewModel.loadEvents()
            eventsViewModel.showAllEvents = false
        }*/

        rootView.locationEdittext.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(rootView.locationEdittext.text)) {
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(rootView.locationEdittext.windowToken, 0)

                eventsViewModel.locationName = rootView.locationEdittext.text.toString()
                eventsViewModel.loadEventsWithAnimation = false
                eventsViewModel.loadLocationEvents()
                return@OnEditorActionListener true
            }
            false
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
        if (eventsViewModel.loadEventsWithAnimation)
            eventsRecyclerAdapter.notifyItemRangeChanged(start, itemsChanged + itemsChanged)
        else
            eventsRecyclerAdapter.notifyDataSetChanged()
    }
}
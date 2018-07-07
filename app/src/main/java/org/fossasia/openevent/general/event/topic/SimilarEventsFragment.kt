package org.fossasia.openevent.general.event.topic

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_similar_events.*
import kotlinx.android.synthetic.main.fragment_similar_events.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.Constants
import org.fossasia.openevent.general.event.*
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class SimilarEventsFragment : Fragment() {
    private val similarEventsRecyclerAdapter: EventsRecyclerAdapter = EventsRecyclerAdapter()
    private val similarEventsViewModel by viewModel<SimilarEventsViewModel>()
    private lateinit var rootView: View
    private var eventTopicId: Long = -1
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        similarEventsRecyclerAdapter.setEventLayout(Constants.SIMILAR_EVENTS)
        val bundle = this.arguments
        var eventId: Long = -1
        if (bundle != null) {
            eventId = bundle.getLong(Constants.EVENT_ID, -1)
            eventTopicId = bundle.getLong(Constants.EVENT_TOPIC_ID, -1)
        }
        similarEventsViewModel.eventId = eventId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_similar_events, container, false)

        linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.similarEventsRecycler.layoutManager = linearLayoutManager

        rootView.similarEventsRecycler.adapter = similarEventsRecyclerAdapter
        rootView.similarEventsRecycler.isNestedScrollingEnabled = false

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(Constants.EVENT_ID, eventID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.rootLayout, fragment)?.addToBackStack(null)?.commit()
            }
        }

        val favouriteFabClickListener = object : FavoriteFabListener {
            override fun onClick(event: Event, isFavourite: Boolean) {
                val id = similarEventsRecyclerAdapter.getPos(event.id)
                similarEventsViewModel.setFavorite(event.id, !isFavourite)
                event.favorite = !event.favorite
                similarEventsRecyclerAdapter.notifyItemChanged(id)
            }
        }

        similarEventsRecyclerAdapter.setListener(recyclerViewClickListener)
        similarEventsViewModel.similarEvents.observe(this, Observer {
            it?.let {
                similarEventsRecyclerAdapter.addAll(it)
                handleVisibility(it)
                similarEventsRecyclerAdapter.notifyDataSetChanged()
            }
            Timber.d("Fetched similar events of size %s", similarEventsRecyclerAdapter.itemCount)
        })

        similarEventsViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        similarEventsViewModel.progress.observe(this, Observer {
            it?.let { Utils.showProgressBar(progressBar, it) }
        })

        similarEventsRecyclerAdapter.setFavorite(favouriteFabClickListener)
        similarEventsViewModel.loadSimilarEvents(eventTopicId)

        return rootView
    }

    fun handleVisibility(similarEvents: List<Event>) {
        if (!similarEvents.isEmpty()) {
            similarEventsDivider.visibility = View.VISIBLE
            moreLikeThis.visibility = View.VISIBLE
            similarEventsRecycler.visibility = View.VISIBLE
        }
    }
}
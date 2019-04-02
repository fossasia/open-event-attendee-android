package org.fossasia.openevent.general.event.topic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_similar_events.moreLikeThis
import kotlinx.android.synthetic.main.fragment_similar_events.progressBar
import kotlinx.android.synthetic.main.fragment_similar_events.similarEventsDivider
import kotlinx.android.synthetic.main.fragment_similar_events.similarEventsRecycler
import kotlinx.android.synthetic.main.fragment_similar_events.view.similarEventsRecycler
import kotlinx.android.synthetic.main.fragment_similar_events.view.similarEventsCoordinatorLayout
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.di.Scopes
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.event.EventDetailsFragmentArgs
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.event.EventsListAdapter
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.common.ShareFabClickListener
import org.fossasia.openevent.general.event.EventLayoutType
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.android.ext.android.get
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SimilarEventsFragment : Fragment() {

    private val similarEventsViewModel by viewModel<SimilarEventsViewModel>()
    private val safeArgs: SimilarEventsFragmentArgs by navArgs()
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var similarEventsListAdapter: EventsListAdapter
    private var similarIdEvents: MutableList<Event> = mutableListOf()
    private var similarLocationEvents: MutableList<Event> = mutableListOf()
    private var similarEvents: MutableList<Event> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindScope(getOrCreateScope(Scopes.SIMILAR_EVENTS_FRAGMENT.toString()))
        similarEventsViewModel.eventId = safeArgs.eventId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_similar_events, container, false)
        similarEventsViewModel.similarLocationEvents
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                similarLocationEvents.clear()
                similarLocationEvents = it.toMutableList()
                Timber.d("Fetched similar location events of size %s", it.size)
                setUpAdapter()
            })

        similarEventsViewModel.similarIdEvents
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                similarIdEvents.clear()
                similarIdEvents = it.toMutableList()
                Timber.d("Fetched similar id events of size %s", it.size)
                setUpAdapter()
            })

        similarEventsViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(rootView.similarEventsCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        similarEventsViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressBar.isVisible = it
            })

        similarEventsViewModel.loadSimilarIdEvents(safeArgs.eventTopicId)
        similarEventsViewModel.loadSimilarLocationEvents(safeArgs.eventLocation.toString())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        similarEventsListAdapter = EventsListAdapter(EventLayoutType.SIMILAR_EVENTS, get())

        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long) {
                EventDetailsFragmentArgs.Builder()
                    .setEventId(eventID)
                    .build()
                    .toBundle()
                    .also { bundle ->
                        findNavController(view).navigate(R.id.eventDetailsFragment, bundle,
                            getAnimSlide())
                    }
            }
        }

        val shareFabClickListener: ShareFabClickListener = object : ShareFabClickListener {
            override fun onClick(event: Event) {
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(event))
                }.also { intent ->
                    startActivity(Intent.createChooser(intent, "Share Event Details"))
                }
            }
        }

        val favFabClickListener: FavoriteFabClickListener = object : FavoriteFabClickListener {
            override fun onClick(event: Event, itemPosition: Int) {
                similarEventsViewModel.setFavorite(event.id, !event.favorite)
                event.favorite = !event.favorite
                similarEventsListAdapter.notifyItemChanged(itemPosition)
            }
        }

        similarEventsListAdapter.apply {
            onEventClick = eventClickListener
            onShareFabClick = shareFabClickListener
            onFavFabClick = favFabClickListener
        }

        linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        view.similarEventsRecycler.layoutManager = linearLayoutManager

        view.similarEventsRecycler.adapter = similarEventsListAdapter
        view.similarEventsRecycler.isNestedScrollingEnabled = false
    }

    private fun handleVisibility(similarEvents: List<Event>) {
        similarEventsDivider.isVisible = !similarEvents.isEmpty()
        moreLikeThis.isVisible = !similarEvents.isEmpty()
        similarEventsRecycler.isVisible = !similarEvents.isEmpty()
    }

    private fun setUpAdapter() {
        similarEvents.clear()
        var id: Long

        when {
            similarIdEvents.size != 0 && similarLocationEvents.size != 0 -> {
                similarIdEvents.forEach {
                    id = it.id
                    if (similarLocationEvents.find { id == it.id } == null) similarEvents.add(it)
                }
                similarEvents.addAll(similarLocationEvents)
            }
            similarIdEvents.size == 0 -> similarEvents.addAll(similarLocationEvents)
            similarLocationEvents.size == 0 -> similarEvents.addAll(similarIdEvents)
        }

        handleVisibility(similarEvents)
        Timber.d("Fetched Similar events of size %s", similarEvents.size)
        if (similarEventsListAdapter.currentList.size != similarEvents.size) similarEvents.shuffle()
        similarEventsListAdapter.submitList(similarEvents)
        similarEventsListAdapter.notifyDataSetChanged()
    }
}

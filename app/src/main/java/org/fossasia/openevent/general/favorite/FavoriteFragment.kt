package org.fossasia.openevent.general.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_favorite.noLikedLL
import kotlinx.android.synthetic.main.fragment_favorite.favoriteCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_favorite.view.favoriteEventsRecycler
import kotlinx.android.synthetic.main.fragment_favorite.view.favoriteProgressBar
import kotlinx.android.synthetic.main.fragment_favorite.view.findText
import kotlinx.android.synthetic.main.fragment_favorite.view.todayChip
import kotlinx.android.synthetic.main.fragment_favorite.view.tomorrowChip
import kotlinx.android.synthetic.main.fragment_favorite.view.weekendChip
import kotlinx.android.synthetic.main.fragment_favorite.view.monthChip
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.di.Scopes
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.event.EventDetailsFragmentArgs
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.common.ShareFabClickListener
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.search.SAVED_LOCATION
import org.fossasia.openevent.general.search.SearchResultsFragmentArgs
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import org.fossasia.openevent.general.utils.Utils.setToolbar

const val FAVORITE_EVENT_DATE_FORMAT: String = "favoriteEventDateFormat"

class FavoriteFragment : Fragment() {
    private val favoriteEventViewModel by viewModel<FavoriteEventsViewModel>()
    private lateinit var rootView: View
    private val favoriteEventsRecyclerAdapter: FavoriteEventsRecyclerAdapter by inject(
        scope = getOrCreateScope(Scopes.FAVORITE_FRAGMENT.toString())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindScope(getOrCreateScope(Scopes.FAVORITE_FRAGMENT.toString()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false)
        rootView.favoriteEventsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.favoriteEventsRecycler.adapter = favoriteEventsRecyclerAdapter
        rootView.favoriteEventsRecycler.isNestedScrollingEnabled = false
        setToolbar(activity, getString(R.string.likes), false)

        rootView.findText.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.eventsFragment, false)
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()
            findNavController(rootView).navigate(R.id.searchFragment, null, navOptions)
        }

        rootView.todayChip.setOnClickListener {
            openSearchResult(rootView.todayChip.text.toString())
        }
        rootView.tomorrowChip.setOnClickListener {
            openSearchResult(rootView.tomorrowChip.text.toString())
        }
        rootView.weekendChip.setOnClickListener {
            openSearchResult(rootView.weekendChip.text.toString())
        }
        rootView.monthChip.setOnClickListener {
            openSearchResult(rootView.monthChip.text.toString())
        }

        favoriteEventViewModel.events
            .nonNull()
            .observe(viewLifecycleOwner, Observer { list ->
                favoriteEventsRecyclerAdapter.submitList(list)
                showEmptyMessage(list.size)
                Timber.d("Fetched events of size %s", list.size)
            })

        favoriteEventViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(favoriteCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        favoriteEventViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.favoriteProgressBar.isIndeterminate = it
                rootView.favoriteProgressBar.isVisible = it
            })

        favoriteEventViewModel.loadFavoriteEvents()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long) { EventDetailsFragmentArgs.Builder()
                .setEventId(eventID)
                .build()
                .toBundle()
                .also { bundle ->
                    findNavController(view).navigate(R.id.eventDetailsFragment, bundle, getAnimFade())
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
                favoriteEventViewModel.setFavorite(event.id, false)
                favoriteEventsRecyclerAdapter.notifyItemChanged(itemPosition)

                Snackbar.make(favoriteCoordinatorLayout,
                    getString(R.string.removed_from_liked, event.name), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.undo)) {
                        favoriteEventViewModel.setFavorite(event.id, true)
                        favoriteEventsRecyclerAdapter.notifyItemChanged(itemPosition)
                    }.show()
            }
        }

        favoriteEventsRecyclerAdapter.apply {
            onEventClick = eventClickListener
            onShareFabClick = shareFabClickListener
            onFavFabClick = favFabClickListener
        }
    }

    private fun showEmptyMessage(itemCount: Int) {
        noLikedLL.isVisible = (itemCount == 0)
    }

    private fun openSearchResult(time: String) {
        SearchResultsFragmentArgs.Builder()
            .setQuery("")
            .setLocation(Preference().getString(SAVED_LOCATION).toString())
            .setDate(time)
            .build()
            .toBundle()
            .also { bundle ->
                findNavController(rootView).navigate(R.id.searchResultsFragment, bundle, Utils.getAnimSlide())
            }
    }
}

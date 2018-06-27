package org.fossasia.openevent.general.favorite

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_favorite.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_ID
import org.fossasia.openevent.general.event.EventDetailsFragment
import org.fossasia.openevent.general.event.FavoriteFabListener
import org.fossasia.openevent.general.event.RecyclerViewClickListener
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber


class FavoriteFragment : Fragment() {
    private val favoriteEventsRecyclerAdapter: FavoriteEventsRecyclerAdapter = FavoriteEventsRecyclerAdapter()
    private val favoriteEventViewModel by viewModel<FavouriteEventsViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false)
        rootView.favoriteEventsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.favoriteEventsRecycler.adapter = favoriteEventsRecyclerAdapter
        rootView.favoriteEventsRecycler.isNestedScrollingEnabled = false
        val dividerItemDecoration = DividerItemDecoration(rootView.favoriteEventsRecycler.context,
                LinearLayoutManager.VERTICAL)
        rootView.favoriteEventsRecycler.addItemDecoration(dividerItemDecoration)

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(EVENT_ID, eventID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.add(R.id.frameContainer, fragment)?.addToBackStack(null)?.commit()
            }
        }
        val favouriteFabClickListener = object : FavoriteFabListener {
            override fun onClick(eventId: Long, isFavourite: Boolean) {
                favoriteEventViewModel.setFavorite(eventId, !isFavourite)
            }
        }

        favoriteEventsRecyclerAdapter.setListener(recyclerViewClickListener)
        favoriteEventsRecyclerAdapter.setFavorite(favouriteFabClickListener)
        favoriteEventViewModel.events.observe(this, Observer {
            it?.let {
                favoriteEventsRecyclerAdapter.addAll(it)
                favoriteEventsRecyclerAdapter.notifyDataSetChanged()
            }
            Timber.d("Fetched events of size %s", favoriteEventsRecyclerAdapter.itemCount)
        })

        favoriteEventViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        favoriteEventViewModel.progress.observe(this, Observer {
            it?.let { showProgressBar(it) }
        })

        favoriteEventViewModel.loadFavoriteEvents()
        return rootView
    }

    private fun showProgressBar(show: Boolean) {
        rootView.favoriteProgressBar.isIndeterminate = show
        rootView.favoriteProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
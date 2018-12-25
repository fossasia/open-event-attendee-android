package org.fossasia.openevent.general.favorite

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.fragment_favorite.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val FAVORITE_EVENT_DATE_FORMAT: String = "favoriteEventDateFormat"

class FavoriteFragment : Fragment() {
    private val favoriteEventsRecyclerAdapter: FavoriteEventsRecyclerAdapter = FavoriteEventsRecyclerAdapter()
    private val favoriteEventViewModel by viewModel<FavouriteEventsViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false)
        rootView.favoriteEventsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.favoriteEventsRecycler.adapter = favoriteEventsRecyclerAdapter
        rootView.favoriteEventsRecycler.isNestedScrollingEnabled = false

        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.title = "Likes"

        val dividerItemDecoration = DividerItemDecoration(rootView.favoriteEventsRecycler.context,
            LinearLayoutManager.VERTICAL)
        rootView.favoriteEventsRecycler.addItemDecoration(dividerItemDecoration)

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(EVENT_ID, eventID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.add(R.id.rootLayout, fragment)?.addToBackStack(null)?.commit()
            }
        }
        val favouriteFabClickListener = object : FavoriteFabListener {
            override fun onClick(event: Event, isFavourite: Boolean) {
                val id = favoriteEventsRecyclerAdapter.getPos(event.id)
                favoriteEventViewModel.setFavorite(event.id, !isFavourite)
                event.favorite = !event.favorite
                favoriteEventsRecyclerAdapter.notifyItemChanged(id)
                showEmptyMessage(favoriteEventsRecyclerAdapter.itemCount)
            }
        }

        favoriteEventsRecyclerAdapter.setListener(recyclerViewClickListener)
        favoriteEventsRecyclerAdapter.setFavorite(favouriteFabClickListener)
        favoriteEventViewModel.events.observe(this, Observer {
            it?.let {
                favoriteEventsRecyclerAdapter.addAll(it)
                favoriteEventsRecyclerAdapter.notifyDataSetChanged()
                showEmptyMessage(favoriteEventsRecyclerAdapter.itemCount)
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

    private fun showEmptyMessage(itemCount: Int) {
        noLikedText.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun showProgressBar(show: Boolean) {
        rootView.favoriteProgressBar.isIndeterminate = show
        rootView.favoriteProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}

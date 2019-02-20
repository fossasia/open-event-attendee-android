package org.fossasia.openevent.general.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_favorite.noLikedText
import kotlinx.android.synthetic.main.fragment_favorite.favoriteCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_favorite.view.favoriteEventsRecycler
import kotlinx.android.synthetic.main.fragment_favorite.view.favoriteProgressBar
import kotlinx.android.synthetic.main.fragment_favorite.view.findSomethingToDo
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_ID
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.FavoriteFabListener
import org.fossasia.openevent.general.event.RecyclerViewClickListener
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val FAVORITE_EVENT_DATE_FORMAT: String = "favoriteEventDateFormat"

class FavoriteFragment : Fragment() {
    private val favoriteEventsRecyclerAdapter: FavoriteEventsRecyclerAdapter = FavoriteEventsRecyclerAdapter()
    private val favoriteEventViewModel by viewModel<FavoriteEventsViewModel>()
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

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = "Likes"
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        rootView.findSomethingToDo.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.eventsFragment, false)
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()
            findNavController(rootView).navigate(R.id.searchFragment, null, navOptions)
        }

        val dividerItemDecoration = DividerItemDecoration(rootView.favoriteEventsRecycler.context,
            LinearLayoutManager.VERTICAL)
        rootView.favoriteEventsRecycler.addItemDecoration(dividerItemDecoration)

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val bundle = Bundle()
                bundle.putLong(EVENT_ID, eventID)
                findNavController(rootView).navigate(R.id.eventDetailsFragment, bundle, getAnimFade())
            }
        }
        val favoriteFabClickListener = object : FavoriteFabListener {
            override fun onClick(event: Event, isFavorite: Boolean) {
                val id = favoriteEventsRecyclerAdapter.getPos(event.id)
                favoriteEventViewModel.setFavorite(event.id, !isFavorite)
                event.favorite = !event.favorite
                favoriteEventsRecyclerAdapter.notifyItemChanged(id)
                showEmptyMessage(favoriteEventsRecyclerAdapter.itemCount)
            }
        }

        favoriteEventsRecyclerAdapter.setListener(recyclerViewClickListener)
        favoriteEventsRecyclerAdapter.setFavorite(favoriteFabClickListener)
        favoriteEventViewModel.events
            .nonNull()
            .observe(this, Observer {
                favoriteEventsRecyclerAdapter.addAll(it)
                favoriteEventsRecyclerAdapter.notifyDataSetChanged()
                showEmptyMessage(favoriteEventsRecyclerAdapter.itemCount)

                Timber.d("Fetched events of size %s", favoriteEventsRecyclerAdapter.itemCount)
            })

        favoriteEventViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(favoriteCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        favoriteEventViewModel.progress
            .nonNull()
            .observe(this, Observer {
                rootView.favoriteProgressBar.isIndeterminate = it
                rootView.favoriteProgressBar.isVisible = it
            })

        favoriteEventViewModel.loadFavoriteEvents()
        return rootView
    }

    private fun showEmptyMessage(itemCount: Int) {
        noLikedText.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
    }
}

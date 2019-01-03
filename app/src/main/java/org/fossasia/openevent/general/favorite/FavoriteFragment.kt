package org.fossasia.openevent.general.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_favorite.noLikedText
import kotlinx.android.synthetic.main.fragment_favorite.view.favoriteEventsRecycler
import kotlinx.android.synthetic.main.fragment_favorite.view.favoriteProgressBar
import kotlinx.android.synthetic.main.fragment_favorite.view.currentDate
import kotlinx.android.synthetic.main.fragment_favorite.view.searchToday
import kotlinx.android.synthetic.main.fragment_favorite.view.searchTomorrow
import kotlinx.android.synthetic.main.fragment_favorite.view.searchWeekend
import kotlinx.android.synthetic.main.fragment_favorite.view.searchNextMonth
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_ID
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDetailsFragment
import org.fossasia.openevent.general.event.FavoriteFabListener
import org.fossasia.openevent.general.event.RecyclerViewClickListener
import org.fossasia.openevent.general.search.SearchTimeViewModel
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Calendar
import java.util.Locale

const val FAVORITE_EVENT_DATE_FORMAT: String = "favoriteEventDateFormat"
const val TODAY = "Today"
const val TOMORROW = "Tomorrow"
const val THIS_WEEKEND = "This Weekend"
const val NEXT_MONTH = "In the next month"

class FavoriteFragment : Fragment() {
    private val searchTimeViewModel by viewModel<SearchTimeViewModel>()
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
                activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.add(R.id.rootLayout, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
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
        val calendar = Calendar.getInstance()
        setCurrentDate(calendar)
        rootView.searchToday.setOnClickListener {
            favoriteEventViewModel.searchToday(calendar, searchTimeViewModel, activity)
        }
        rootView.searchTomorrow.setOnClickListener {
            favoriteEventViewModel.searchTomorrow(calendar, searchTimeViewModel, activity)
        }
        rootView.searchWeekend.setOnClickListener {
            favoriteEventViewModel.searchWeekend(calendar, searchTimeViewModel, activity)
        }
        rootView.searchNextMonth.setOnClickListener {
            favoriteEventViewModel.searchNextMonth(calendar, searchTimeViewModel, activity)
        }

        favoriteEventsRecyclerAdapter.setListener(recyclerViewClickListener)
        favoriteEventsRecyclerAdapter.setFavorite(favouriteFabClickListener)
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
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
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

    fun setCurrentDate(calendar: Calendar) {
        val dayLongName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val date = calendar.get(Calendar.DAY_OF_MONTH)
        val complete_date = "Today - $dayLongName, $month $date"
        rootView.currentDate.text = complete_date
    }
}
